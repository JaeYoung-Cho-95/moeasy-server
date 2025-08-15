# test_clova_once.py
import requests, uuid, sys, json
API_KEY = "nv-ea00f37975da413595b8ded2f7ce71a9Aqu7"
ENDPOINT = "https://clovastudio.stream.ntruss.com/v1/api-tools/embedding/v2/"

text = '{"error": "CLOVA 임베딩 반복 실패", "raw": "{\"id\":\"DIGITAL:12:v1\",\"category\":\"DIGITAL\",\"question\":\"배송/반품 프로세스는 어떻게 연동되나요?\",\"answers\":[\"자체 물류 연동\",\"풀필먼트 위탁\",\"마켓 규칙 준용\",\"오프라인 픽업(Pick-up)\",\"미정\"],\"tags\":[\"digital\",\"fashion\",\"beauty\",\"logistics\",\"returns\",\"배송\",\"반품\",\"픽업\"],\"answer_policy\":{\"type\":\"fixed\",\"extendable\":true,\"must_include_one_of\":[\"미정\",\"기타\"],\"max_options\":5},\"locale\":\"ko-KR\"}\n"}'
r = requests.post(
    ENDPOINT,
    headers={
        "Authorization": f"Bearer {API_KEY}",
        "X-NCP-CLOVASTUDIO-REQUEST-ID": str(uuid.uuid4()),
        "Content-Type": "application/json",
    },
    json={"text": text},
    timeout=30
)
print("STATUS:", r.status_code)
print("HEADERS:", dict(r.headers))
print("BODY:", r.text[:1000])
try:
    print("PARSED:", json.loads(r.text))
except Exception:
    pass
