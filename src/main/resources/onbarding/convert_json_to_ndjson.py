import argparse
import json
import re
import sys
import unicodedata
from typing import Any, Dict, List, IO, Optional, Tuple

CATEGORIES = ["CONTENT", "BRAND", "SHOP", "EVENT", "DIGITAL", "PRODUCT"]

ID_RE = re.compile(r"^(?P<cat>[A-Z]+):(?P<num>\d+)(?::v(?P<ver>\d+))?$")


def _load_json(stream: IO[str]) -> Dict[str, Any]:
  try:
    return json.load(stream)
  except json.JSONDecodeError as e:
    print(f"[ERROR] 잘못된 JSON 형식입니다: {e}", file=sys.stderr)
    sys.exit(1)


def _validate_questions(data: Dict[str, Any]) -> List[Any]:
  if "questions" not in data:
    print("[ERROR] 최상위에 'questions' 키가 없습니다.", file=sys.stderr)
    sys.exit(1)
  questions = data["questions"]
  if not isinstance(questions, list):
    print("[ERROR] 'questions' 값은 배열이어야 합니다.", file=sys.stderr)
    sys.exit(1)
  return questions


def _parse_id(id_str: str) -> Optional[Tuple[str, int, Optional[str]]]:
  m = ID_RE.match(id_str)
  if not m:
    return None
  cat = m.group("cat")
  num = int(m.group("num"))
  ver = m.group("ver")
  return cat, num, ver


def _infer_category(item: Any) -> Optional[str]:
  """item에서 카테고리를 추론한다. 우선순위: item['category'] -> item['id'] 접두어"""
  if isinstance(item, dict):
    cat = item.get("category")
    if isinstance(cat, str) and cat in CATEGORIES:
      return cat
    id_val = item.get("id")
    if isinstance(id_val, str):
      parsed = _parse_id(id_val)
      if parsed:
        cat_from_id, _, _ = parsed
        if cat_from_id in CATEGORIES:
          return cat_from_id
  return None


def _rewrite_id(cat: str, new_index: int, old_id: Optional[str]) -> str:
  """카테고리/버전 유지, 숫자만 교체. 버전 없으면 v1 사용."""
  ver = "1"
  if isinstance(old_id, str):
    parsed = _parse_id(old_id)
    if parsed and parsed[2] is not None:
      ver = parsed[2]
  return f"{cat}:{new_index}:v{ver}"


def _normalize_question_value(val: Any) -> Optional[str]:
  """중복 판정을 위한 정규화: NFC, trim, 내부 공백 1칸."""
  if val is None:
    return None
  s = unicodedata.normalize("NFC", str(val))
  s = s.strip()
  s = re.sub(r"\s+", " ", s)
  return s if s else None


def _dedupe_questions(questions: List[Any]) -> List[Any]:
  """카테고리+도메인+question 기준으로 중복 제거(첫 등장만 유지)."""
  seen: Set[Tuple[str, str, str]] = set()
  result: List[Any] = []

  def norm(x: Any) -> str:
    s = _normalize_question_value(x)
    return s if s is not None else ""

  for q in questions:
    if isinstance(q, dict) and "question" in q:
      cat = _infer_category(q) or ""
      dom = norm(q.get("domain"))
      qtext = norm(q.get("question"))
      key = (cat, dom, qtext)

      if qtext and key in seen:
        continue
      if qtext:
        seen.add(key)
      result.append(q)
    else:
      # dict가 아니거나 question이 없으면 그대로 통과
      result.append(q)

  return result


def _group_and_renumber(questions: List[Any]) -> List[Any]:
  """카테고리별로 묶고, 각 카테고리 내부는 입력 등장 순서 유지하면서 id 숫자를 1..N으로 재부여."""
  groups: Dict[str, List[Any]] = {cat: [] for cat in CATEGORIES}
  others: List[Any] = []

  # 그룹 나누기 (입력 순서 유지)
  for q in questions:
    cat = _infer_category(q)
    if cat is None:
      others.append(q)
    else:
      groups[cat].append(q)

  # 카테고리 순서대로 재번호 및 결과 합치기
  result: List[Any] = []
  for cat in CATEGORIES:
    idx = 1
    for item in groups[cat]:
      if isinstance(item, dict):
        old_id = item.get("id")
        new_item = dict(item)  # 원본 변형 방지
        new_item["id"] = _rewrite_id(cat, idx, old_id)
        new_item["category"] = cat
      else:
        new_item = {"value": item, "id": _rewrite_id(cat, idx, None), "category": cat}
      result.append(new_item)
      idx += 1

  # 카테고리 판별 실패 항목은 맨 뒤에 원본 그대로
  result.extend(others)
  return result


def _write_ndjson(
    questions: List[Any],
    out: IO[str],
    top_level: Optional[Dict[str, Any]],
    merge_top_level: bool,
) -> None:
  for item in questions:
    if merge_top_level:
      # 상위 필드를 병합하되, questions는 제외
      base = {} if top_level is None else {k: v for k, v in top_level.items() if k != "questions"}
      if isinstance(item, dict):
        line_obj = {**base, **item}
      else:
        line_obj = {**base, "value": item}
    else:
      line_obj = item
    json.dump(line_obj, out, ensure_ascii=False, separators=(",", ":"))
    out.write("\n")


def main(argv: List[str]) -> int:
  parser = argparse.ArgumentParser(
      description=(
          "입력 JSON의 questions 배열을 카테고리별로 묶어 NDJSON으로 변환합니다. "
          "중복 question은 제거하고, 각 카테고리 내부는 등장 순서 유지, "
          "id는 {CATEGORY}:{1..N}:vX 형식으로 재부여합니다."
      )
  )
  parser.add_argument(
      "input",
      nargs="?",
      default="-",
      help="입력 파일 경로(기본: 표준입력, '-' 지원)",
  )
  parser.add_argument(
      "-o",
      "--output",
      default="-",
      help="출력 파일 경로(기본: 표준출력, '-' 지원)",
  )
  parser.add_argument(
      "--merge-top-level",
      action="store_true",
      help="각 라인에 상위 필드(doc_id, version 등)를 병합합니다(questions는 제외).",
  )
  args = parser.parse_args(argv)

  # 입력 열기
  if args.input == "-":
    data = _load_json(sys.stdin)
  else:
    try:
      with open(args.input, "r", encoding="utf-8") as f:
        data = _load_json(f)
    except OSError as e:
      print(f"[ERROR] 입력 파일을 열 수 없습니다: {e}", file=sys.stderr)
      return 1

  questions = _validate_questions(data)

  # 1) 중복 제거 -> 2) 카테고리 묶기/재번호
  deduped = _dedupe_questions(questions)
  processed = _group_and_renumber(deduped)

  # 출력 열기
  if args.output == "-":
    out = sys.stdout
    close_out = False
  else:
    try:
      out = open(args.output, "w", encoding="utf-8")
      close_out = True
    except OSError as e:
      print(f"[ERROR] 출력 파일을 열 수 없습니다: {e}", file=sys.stderr)
      return 1

  try:
    _write_ndjson(
        questions=processed,
        out=out,
        top_level=data if args.merge_top_level else None,
        merge_top_level=args.merge_top_level,
    )
  except BrokenPipeError:
    # 파이프가 끊긴 경우 조용히 종료
    return 0


if __name__ == "__main__":
  sys.exit(main(sys.argv[1:]))
