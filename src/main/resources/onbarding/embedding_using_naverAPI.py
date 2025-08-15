# -*- coding: utf-8 -*-
"""
NDJSON(문항) -> CLOVA Embedding -> 임베딩 JSONL로 저장
- 환경변수 미사용: --api-key 로 전달
- 입력: survey.ndjson  (각 라인: {id, category, question, answers, tags, ...})
- 출력: survey.embeddings.ndjson  (각 라인: {id, category, vector, payload, search_text})
"""

import argparse, json, time, uuid, os
from typing import List, Dict, Any
import requests
from tqdm import tqdm

def build_search_text(r: Dict[str, Any]) -> str:
    parts: List[str] = []
    parts.append(str(r.get("question", "")))
    parts += [str(t) for t in r.get("tags", [])]
    parts += [str(a) for a in r.get("answers", [])]
    parts.append(str(r.get("category", "")))
    return " ".join(parts).strip()

def parse_embedding_from_response(data: Dict[str, Any]) -> List[float]:
    # 응답 스키마 변화 대비
    if "embedding" in data and isinstance(data["embedding"], list):
        return data["embedding"]
    if "result" in data and isinstance(data["result"], dict):
        if "embedding" in data["result"]:
            return data["result"]["embedding"]
        if "vector" in data["result"]:
            return data["result"]["vector"]
    raise KeyError(f"임베딩 벡터 키를 찾지 못함. 응답 일부: {str(data)[:200]}")

def clova_embed(text: str, api_key: str, endpoint: str, timeout: float,
                max_retries: int = 5, backoff: float = 1.5) -> List[float]:
    headers = {
        "Authorization": f"Bearer {api_key}",
        "X-NCP-CLOVASTUDIO-REQUEST-ID": str(uuid.uuid4()),
        "Content-Type": "application/json",
    }
    payload = {"text": text}
    for attempt in range(1, max_retries + 1):
        time.sleep(0.5)
        try:
            r = requests.post(endpoint, headers=headers, json=payload, timeout=timeout)
            if r.status_code == 200:
                j = r.json()
                vec = parse_embedding_from_response(j)
                if not isinstance(vec, list):
                    raise ValueError("임베딩 형식 오류")
                return [float(x) for x in vec]
            if r.status_code in (429, 500, 502, 503, 504):
                time.sleep(backoff ** (attempt - 1))
                continue
            raise RuntimeError(f"CLOVA API {r.status_code}: {r.text[:200]}")
        except requests.RequestException:
            if attempt == max_retries:
                raise
            time.sleep(backoff ** (attempt - 1))
    raise RuntimeError("CLOVA 임베딩 반복 실패")

def main():
    ap = argparse.ArgumentParser(description="NDJSON -> CLOVA 임베딩 -> 임베딩 JSONL 저장")
    ap.add_argument("--in", dest="src", required=True, help="입력 NDJSON (문항)")
    ap.add_argument("--out", dest="dst", default="survey.embeddings.ndjson", help="출력 임베딩 JSONL")
    ap.add_argument("--api-key", required=True, help="CLOVA API Key (Bearer)")
    ap.add_argument("--endpoint", default="https://clovastudio.stream.ntruss.com/v1/api-tools/embedding/v2/",
                    help="CLOVA 임베딩 엔드포인트")
    ap.add_argument("--timeout", type=float, default=30.0)
    ap.add_argument("--resume", action="store_true", help="이미 생성된 out 파일을 읽어 이어서 진행(중복 건 스킵)")
    ap.add_argument("--errors", default="survey.embeddings.errors.ndjson", help="실패 레코드 기록 파일")
    args = ap.parse_args()

    # (옵션) resume 지원: 이미 처리한 id 스킵
    done_ids = set()
    if args.resume and os.path.exists(args.dst):
        with open(args.dst, encoding="utf-8") as f:
            for ln in f:
                ln = ln.strip()
                if not ln:
                    continue
                try:
                    j = json.loads(ln)
                    done_ids.add(j["id"])
                except Exception:
                    pass

    with open(args.src, encoding="utf-8") as f:
        lines = [ln for ln in f if ln.strip()]

    out = open(args.dst, "a", encoding="utf-8")
    err = open(args.errors, "a", encoding="utf-8")

    pbar = tqdm(total=len(lines), desc="Embedding", unit="rec")
    for ln in lines:
        pbar.update(1)
        try:
            rec = json.loads(ln)
            rid = rec["id"]
            if args.resume and rid in done_ids:
                continue
            text = build_search_text(rec)
            vec = clova_embed(text, api_key=args.api_key, endpoint=args.endpoint, timeout=args.timeout)
            out_obj = {
                "id": rid,
                "category": rec.get("category", ""),
                "domain": rec.get("domain", ""),
                "vector": vec,
                "payload": rec,           # 원본 전체
                "search_text": text       # 디버그용(검색 텍스트)
            }
            out.write(json.dumps(out_obj, ensure_ascii=False) + "\n")
            out.flush()
        except Exception as e:
            err_obj = {"error": str(e), "raw": ln[:500]}
            err.write(json.dumps(err_obj, ensure_ascii=False) + "\n")
            err.flush()

    pbar.close()
    out.close()
    err.close()
    print(f"완료: {args.dst} (오류는 {args.errors} 참고)")

if __name__ == "__main__":
    main()