# -*- coding: utf-8 -*-
"""
Milvus 삭제 유틸
- 컬렉션 전체 삭제 / 파티션 삭제 / 조건(expr) 삭제 / ID 목록 삭제 지원
- 옵션으로 compaction(디스크 회수) 트리거 및 대기
"""

import argparse
import time
from typing import List

from pymilvus import connections, Collection, utility


def build_id_expr(ids: List[str]) -> str:
    # "id in ["A","B","C"]" 형태의 expr 생성
    quoted = ",".join([f"\"{i}\"" for i in ids])
    return f"id in [{quoted}]"


def main():
    ap = argparse.ArgumentParser(description="Milvus 삭제 스크립트")
    ap.add_argument("--host", default="127.0.0.1")
    ap.add_argument("--port", default="19530")
    ap.add_argument("--collection", required=True, help="컬렉션 이름(예: survey)")

    # 삭제 모드 (서로 배타적)
    grp = ap.add_mutually_exclusive_group(required=True)
    grp.add_argument("--drop-collection", action="store_true", help="컬렉션 자체 삭제")
    grp.add_argument("--drop-partition", help="해당 파티션만 삭제")
    grp.add_argument("--expr", help='삭제 조건 expr (예: category == "SHOP")')
    grp.add_argument("--ids", help="삭제할 ID들(쉼표 구분, 예: A,B,C)")
    grp.add_argument("--ids-file", help="삭제할 ID 목록 파일(한 줄에 하나)")

    # 컴팩션
    ap.add_argument("--compact", action="store_true", help="삭제 후 compaction 트리거")
    ap.add_argument("--wait-compact", action="store_true", help="compaction 완료까지 대기(권장 아님)")
    ap.add_argument("--poll-interval", type=float, default=2.0, help="대기 시 폴링 간격(sec)")

    args = ap.parse_args()

    # 연결
    connections.connect(host=args.host, port=args.port)

    # 드롭 컬렉션
    if args.drop_collection:
        if utility.has_collection(args.collection):
            utility.drop_collection(args.collection)
            print(f"[OK] 컬렉션 드롭: {args.collection}")
        else:
            print(f"[SKIP] 컬렉션 없음: {args.collection}")
        return

    # 컬렉션 존재 확인
    if not utility.has_collection(args.collection):
        raise SystemExit(f"[ERR] 컬렉션이 없습니다: {args.collection}")

    # 드롭 파티션
    if args.drop_partition:
        part = args.drop_partition
        if utility.has_partition(args.collection, part):
            utility.drop_partition(args.collection, part)
            print(f"[OK] 파티션 드롭: {args.collection}/{part}")
        else:
            print(f"[SKIP] 파티션 없음: {args.collection}/{part}")
        return

    # 조건/ID 삭제
    expr = None
    if args.expr:
        expr = args.expr
    elif args.ids:
        ids_list = [x.strip() for x in args.ids.split(",") if x.strip()]
        if not ids_list:
            raise SystemExit("[ERR] --ids 에 유효한 값이 없습니다.")
        expr = build_id_expr(ids_list)
    elif args.ids_file:
        with open(args.ids_file, encoding="utf-8") as f:
            ids_list = [ln.strip() for ln in f if ln.strip()]
        if not ids_list:
            raise SystemExit("[ERR] --ids-file 에 유효한 ID가 없습니다.")
        expr = build_id_expr(ids_list)

    if not expr:
        raise SystemExit("[ERR] 삭제 expr을 구성하지 못했습니다.")

    col = Collection(args.collection)
    # 삭제 실행
    mr = col.delete(expr=expr)
    # mr.delete_count 는 버전/상황에 따라 없을 수 있음. mutation 결과 출력
    try:
        print(f"[OK] delete 요청 완료. result: {mr}")
    except Exception:
        print("[OK] delete 요청 완료.")

    # compaction
    if args.compact:
        compaction_id = utility.compact(args.collection)
        print(f"[OK] compaction 트리거: {compaction_id}")
        if args.wait_compact:
            # 상태 폴링
            while True:
                st = utility.get_compaction_state(compaction_id)
                # st 형태는 버전에 따라 다를 수 있음; 'state' 필드가 2(Completed)면 완료로 보는 경우가 많음
                print(f"[WAIT] compaction state: {st}")
                # 보수적으로 문자열 포함 체크
                s = str(st).lower()
                if "completed" in s or "2" in s:
                    print("[OK] compaction 완료")
                    break
                time.sleep(args.poll_interval)

    print("[DONE] 삭제 작업 종료")


if __name__ == "__main__":
    main()
