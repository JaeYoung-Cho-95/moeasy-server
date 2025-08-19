import os, re, glob, json, time, random, requests, pandas as pd
from typing import Dict, Any, Tuple

# =========================
# 1) 네이버 HCX-007 API 설정
# =========================
BASE_URL = "https://clovastudio.stream.ntruss.com"
API_PATH = "/v3/chat-completions/HCX-007"

HEADERS = {
    "Authorization": "Bearer nv-ea00f37975da413595b8ded2f7ce71a9Aqu7",
    "Content-Type": "application/json; charset=utf-8",
}

# =========================
# 2) 파일 경로
# =========================
SYSTEM_PROMPT_FILE = "./system_prompt.txt"          # Text 생성용 '시스템' 프롬프트
TEXT_PROMPTS_PATH  = "./make_text_prompt.txt"       # Text 생성용 '유저' 프롬프트(파일 or 폴더)
COMPLETION_SYSTEM_PROMPT_FILE = "./make_complete_prompt.txt"  # Completion 생성용 '시스템' 프롬프트
OUTPUT_CSV = "survey_dataset.csv"
FAIL_LOG = "failures.json"


DEBUG_LOG = False         
PAIR_RETRIES = 4         
PAIR_BACKOFF = 0.4        
LIMIT = 300                

def call_naver_hcx007(system_prompt: str,
                      user_prompt: str,
                      top_p: float = 0.8,
                      top_k: int = 0,
                      max_tokens: int = 4096,
                      temperature: float = 0.5,
                      timeout: int = 240,
                      retries: int = 3,
                      backoff_base: float = 1.5) -> Dict[str, Any]:
    url = f"{BASE_URL}{API_PATH}"
    payload = {
        "messages": [
            {"role": "system", "content": [{"type": "text", "text": system_prompt}]},
            {"role": "user",   "content": [{"type": "text", "text": user_prompt}]}
        ],
        "topP": top_p,
        "topK": top_k,
        "maxCompletionTokens": max_tokens,
        "temperature": temperature,
        "stop": []
    }

    last_err = None
    for attempt in range(1, retries + 1):
        try:
            resp = requests.post(url, headers=HEADERS, data=json.dumps(payload), timeout=timeout)
            if not resp.ok:
                raise RuntimeError(f"API call failed: HTTP {resp.status_code} // {resp.text[:500]}")
            return resp.json()
        except Exception as e:
            last_err = e
            if attempt == retries:
                raise
            time.sleep((backoff_base ** (attempt - 1)) + random.random() * 0.3)
    raise RuntimeError(f"API call failed after retries: {last_err}")

# =========================
# 4) 응답 파싱 유틸
# =========================
def extract_text_content(resp: Dict[str, Any]) -> str:
    """
    네이버 HCX 응답에서 실제 텍스트를 안전하게 추출.
    우선순위:
    1) result.message.content (문자열 또는 파트 배열)
    2) choices[0].message.content (OpenAI 호환)
    3) result.output_text
    4) 기타 필드
    """
    try:
        msg = resp.get("result", {}).get("message", {})
        if isinstance(msg, dict):
            c = msg.get("content")
            if isinstance(c, str) and c.strip():
                return c
            if isinstance(c, list):
                parts = []
                for p in c:
                    if isinstance(p, dict) and p.get("type") == "text" and isinstance(p.get("text"), str):
                        parts.append(p["text"])
                if parts:
                    return "\n".join(parts)
    except Exception:
        pass
    try:
        return resp["choices"][0]["message"]["content"]
    except Exception:
        pass
    try:
        return resp["result"]["output_text"]
    except Exception:
        pass
    for k in ("content", "message", "output", "data"):
        v = resp.get(k)
        if isinstance(v, str):
            return v
        if isinstance(v, dict) and isinstance(v.get("content"), str):
            return v["content"]
    return json.dumps(resp, ensure_ascii=False)

def strip_code_fences(s: str) -> str:
    return re.sub(r"^```[\s\S]*?\n|```$", "", s.strip())

def sanitize_json_like(text: str) -> str:
    t = text
    t = t.replace("“", '"').replace("”", '"').replace("‘", "'").replace("’", "'")
    t = t.replace("\ufeff", "").replace("\u200b", "").replace("\u200c", "").replace("\u200d", "")
    key_pat = re.compile(
        r'(?m)(?<=\{|\,)\s*(?!")([A-Za-z0-9\u3131-\u318F\uAC00-\uD7A3 _./~!?()\-\[\]{}]+?)\s*:',
        re.UNICODE
    )
    t = key_pat.sub(lambda m: f'"{m.group(1).strip()}":', t)
    t = re.sub(r'(?<!")기타"\s*:', r'"기타":', t)
    t = re.sub(r',\s*([\]\}])', r'\1', t)
    return t

def ensure_json(text: str):
    raw = strip_code_fences(text).strip()
    # 1차
    try:
        return json.loads(raw)
    except json.JSONDecodeError:
        pass
    # 2차
    sanitized = sanitize_json_like(raw)
    try:
        return json.loads(sanitized)
    except json.JSONDecodeError:
        pass
    # 3차: 배열만 추출
    for cand in (raw, sanitized):
        fb, lb = cand.find('['), cand.rfind(']')
        if fb != -1 and lb != -1 and lb > fb:
            arr = cand[fb:lb+1]
            try:
                return json.loads(arr)
            except json.JSONDecodeError:
                pass
    # 4차: 객체만 추출
    for cand in (raw, sanitized):
        fo, lo = cand.find('{'), cand.rfind('}')
        if fo != -1 and lo != -1 and lo > fo:
            obj = cand[fo:lo+1]
            try:
                return json.loads(obj)
            except json.JSONDecodeError:
                pass
    # 실패 시 디버그 덤프
    ts = int(time.time() * 1000)
    with open(f"_text_raw_debug_{ts}.txt", "w", encoding="utf-8") as f:
        f.write(text)
    raise ValueError("응답이 JSON으로 파싱되지 않았습니다. _text_raw_debug_* 파일을 확인하세요.")

# =========================
# 5) 검증 & 페어 생성 (원자성 보장)
# =========================
def is_valid_text(obj) -> bool:
    # Text는 반드시 '비어있지 않은 리스트'
    return isinstance(obj, list) and len(obj) > 0

def is_valid_completion(obj) -> bool:
    # Completion은 반드시 '비어있지 않은 딕셔너리'
    return isinstance(obj, dict) and len(obj) > 0

def generate_valid_pair(system_prompt_for_text: str,
                        text_user_prompt: str,
                        completion_system_prompt: str,
                        pair_retries: int = PAIR_RETRIES) -> Tuple[list, dict]:
    """
    1) Text 생성 → 검증
    2) (검증 통과한 Text로) Completion 생성 → 검증
    실패 시 'Text부터' 다시 시작(원자성 보장).
    """
    last_err = None
    for attempt in range(1, pair_retries + 1):
        try:
            # --- A. Text 생성 ---
            resp_text = call_naver_hcx007(system_prompt_for_text, text_user_prompt)
            if DEBUG_LOG:
                print(f"[pair#{attempt}] raw TEXT resp:", json.dumps(resp_text, ensure_ascii=False)[:3000])
            text_raw = strip_code_fences(extract_text_content(resp_text))
            parsed_text = ensure_json(text_raw)
            if not is_valid_text(parsed_text):
                raise ValueError(f"[pair#{attempt}] invalid TEXT payload (type/empty)")

            # --- B. Completion 생성 (Text JSON을 유저 프롬프트로) ---
            text_json_str = json.dumps(parsed_text, ensure_ascii=False)
            resp_completion = call_naver_hcx007(completion_system_prompt, text_json_str)
            if DEBUG_LOG:
                print(f"[pair#{attempt}] raw COMPLETION resp:", json.dumps(resp_completion, ensure_ascii=False)[:3000])
            completion_raw = strip_code_fences(extract_text_content(resp_completion))
            parsed_completion = ensure_json(completion_raw)
            if not is_valid_completion(parsed_completion):
                raise ValueError(f"[pair#{attempt}] invalid COMPLETION payload (type/empty)")

            # 둘 다 유효 → 성공 페어 반환
            return parsed_text, parsed_completion

        except Exception as e:
            last_err = e
            if DEBUG_LOG:
                print(f"[pair#{attempt}] failed: {e}")
            # Text부터 다시 시도
            time.sleep(PAIR_BACKOFF * attempt)

    # 모든 시도 실패
    raise RuntimeError(f"pair generation failed after {pair_retries} attempts: {last_err}")

# =========================
# 6) 입출력 유틸
# =========================
def read_text_or_dir(path: str):
    """단일 파일이면 [파일경로], 폴더면 내부 *.txt 리스트 반환"""
    if os.path.isfile(path):
        return [path]
    return sorted(glob.glob(os.path.join(path, "*.txt")))

def append_row_to_csv(row: dict, csv_path: str):
    """유효한 한 행만 즉시 append 저장 (중도 실패가 이전 행에 영향 주지 않도록)"""
    is_new = not os.path.exists(csv_path) or os.path.getsize(csv_path) == 0
    df_one = pd.DataFrame([row], columns=["System_Prompt", "C_ID", "T_ID", "Text", "Completion"])
    df_one.to_csv(csv_path, index=False, encoding="utf-8-sig", mode="a", header=is_new)

# =========================
# 7) 메인 파이프라인
# =========================
def main():
    # (1) 프롬프트 로드
    with open(SYSTEM_PROMPT_FILE, "r", encoding="utf-8") as f:
        SYSTEM_PROMPT_FOR_TEXT = f.read().strip()
    with open(COMPLETION_SYSTEM_PROMPT_FILE, "r", encoding="utf-8") as f:
        COMPLETION_SYSTEM_PROMPT = f.read().strip()

    text_prompt_files = read_text_or_dir(TEXT_PROMPTS_PATH)

    # 단일 파일이면 같은 파일을 LIMIT만큼 반복
    if os.path.isfile(TEXT_PROMPTS_PATH):
        if LIMIT is None:
            raise ValueError("단일 파일 반복 처리에는 LIMIT가 필요합니다.")
        text_prompt_files = text_prompt_files * LIMIT

    failures = []
    cid = 1
    processed = 0

    for i, path in enumerate(text_prompt_files):
        if LIMIT is not None and i >= LIMIT:
            break

        try:
            with open(path, "r", encoding="utf-8") as f:
                TEXT_USER_PROMPT = f.read().strip()

            # --- Text/Completion '원자적' 페어 생성 ---
            parsed_text, parsed_completion = generate_valid_pair(
                SYSTEM_PROMPT_FOR_TEXT,
                TEXT_USER_PROMPT,
                COMPLETION_SYSTEM_PROMPT
            )

            # --- 유효한 페어만 CSV에 append ---
            row = {
                "System_Prompt": SYSTEM_PROMPT_FOR_TEXT,
                "C_ID": cid,
                "T_ID": 0,
                "Text": json.dumps(parsed_text, ensure_ascii=False),
                "Completion": json.dumps(parsed_completion, ensure_ascii=False)
            }
            append_row_to_csv(row, OUTPUT_CSV)

            cid += 1
            processed += 1

            # 호출 과속 방지 (선택)
            time.sleep(0.05)

        except Exception as e:
            failures.append({"index": i, "path": path, "error": str(e)})
            # 다음 항목으로 계속
            continue

    # 실패 로그 저장
    if failures:
        with open(FAIL_LOG, "w", encoding="utf-8") as f:
            json.dump(failures, f, ensure_ascii=False, indent=2)

    print(f"✅ 처리 완료: 성공 {processed}건, 실패 {len(failures)}건 → {OUTPUT_CSV}")
    if failures:
        print(f"⚠️ 실패 상세는 {FAIL_LOG} 확인")

if __name__ == "__main__":
    main()