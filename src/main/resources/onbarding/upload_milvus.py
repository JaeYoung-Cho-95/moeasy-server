"""
임베딩 JSONL -> Milvus 업로드
- 입력: survey.embeddings.ndjson (각 라인: {id, category, vector, payload})
- Milvus 컬렉션 스키마:
  id(VARCHAR, PK), category(VARCHAR), vector(FLOAT_VECTOR), payload(JSON)
"""

import argparse, json
from typing import List, Dict, Any, Tuple

from pymilvus import (
    connections, FieldSchema, CollectionSchema, DataType,
    Collection, utility
)

def ensure_collection(name: str, dim: int, recreate: bool,
                      index_type: str = "IVF_FLAT", metric: str = "COSINE") -> Collection:
    if recreate and utility.has_collection(name):
        utility.drop_collection(name)

    if not utility.has_collection(name):
        fields = [
            FieldSchema(name="id", dtype=DataType.VARCHAR, is_primary=True, auto_id=False, max_length=128),
            FieldSchema(name="category", dtype=DataType.VARCHAR, max_length=64),
            FieldSchema(name="domain", dtype=DataType.VARCHAR, max_length=64),
            FieldSchema(name="vector", dtype=DataType.FLOAT_VECTOR, dim=dim),
            FieldSchema(name="payload", dtype=DataType.JSON),
        ]
        schema = CollectionSchema(fields=fields, description="Survey questions (RAG)")
        col = Collection(name=name, schema=schema)
        index_params = {"index_type": index_type, "metric_type": metric, "params": {"nlist": 1024}}
        col.create_index(field_name="vector", index_params=index_params)
        col.load()
        return col

    col = Collection(name)
    if not col.has_index():
        index_params = {"index_type": index_type, "metric_type": metric, "params": {"nlist": 1024}}
        col.create_index(field_name="vector", index_params=index_params)
    col.load()
    return col

def insert_batch(col: Collection, batch: List[Dict[str, Any]]):
    ids = [r["id"] for r in batch]
    cats = [str(r.get("category", "")) for r in batch]
    # ↓ payload.domain을 자동으로 끌어올리는 fallback
    def get_domain(rec):
        if rec.get("domain") not in (None, ""):
            return str(rec["domain"])
        pl = rec.get("payload")
        if isinstance(pl, dict) and pl.get("domain") not in (None, ""):
            return str(pl["domain"])
        return ""  # 없으면 빈 문자열
    doms = [get_domain(r) for r in batch]
    vecs = [r["vector"] for r in batch]
    payloads = [r["payload"] for r in batch]
    col.insert([ids, cats, doms, vecs, payloads])

def main():
    ap = argparse.ArgumentParser(description="임베딩 JSONL -> Milvus 업로드")
    ap.add_argument("--in", dest="src", required=True, help="임베딩 JSONL (ex. survey.embeddings.ndjson)")
    ap.add_argument("--collection", default="survey")
    ap.add_argument("--host", default="127.0.0.1")
    ap.add_argument("--port", default="19530")
    ap.add_argument("--recreate", action="store_true", help="컬렉션 삭제 후 재생성")
    ap.add_argument("--batch", type=int, default=512)
    ap.add_argument("--index-type", default="IVF_FLAT", choices=["IVF_FLAT","HNSW","IVF_SQ8","DISKANN"])
    ap.add_argument("--metric", default="COSINE", choices=["COSINE","L2","IP"])
    args = ap.parse_args()

    connections.connect(host=args.host, port=args.port)

    # 첫 라인에서 차원 확인
    with open(args.src, encoding="utf-8") as f:
        first_line = None
        for ln in f:
            if ln.strip():
                first_line = json.loads(ln)
                break
    if not first_line:
        raise SystemExit("입력 파일에 유효한 레코드가 없습니다.")

    dim = len(first_line["vector"])
    col = ensure_collection(args.collection, dim, args.recreate, args.index_type, args.metric)

    # 첫 라인 포함 전체 업로드
    batch = [first_line]
    total = 0

    with open(args.src, encoding="utf-8") as f:
        for ln in f:
            ln = ln.strip()
            if not ln:
                continue
            rec = json.loads(ln)
            # 첫 라인을 중복 삽입하지 않도록 체크
            if total == 0 and rec["id"] == first_line["id"]:
                continue
            batch.append(rec)
            if len(batch) >= args.batch:
                insert_batch(col, batch)
                total += len(batch)
                batch.clear()

    if batch:
        insert_batch(col, batch)
        total += len(batch)

    col.flush()
    print(f"Milvus 업로드 완료: {total}개 (collection={args.collection}, dim={dim}, index={args.index_type}/{args.metric})")

if __name__ == "__main__":
    main()
