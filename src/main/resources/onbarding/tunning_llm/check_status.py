import requests
import json

def call_tuning_api():
    # API 엔드포인트
    url = "https://clovastudio.stream.ntruss.com/tuning/v2/tasks/efa39b52"
    
    # 헤더 설정
    headers = {
        "Content-Type": "application/json",
        "Authorization": "Bearer nv-ea00f37975da413595b8ded2f7ce71a9Aqu7"
    }
    
    # 요청 본문
    payload = {
        "name": "test_v_01",
        "model": "HCX-005",
        "trainEpochs": 10,
        "trainingDatasetBucket": "moeasy",
        "trainingDatasetFilePath": "survey_dataset.csv",
        "trainingDatasetAccessKey": "ncp_iam_BPAMKR3CzYPAUgCm0y0s",
        "trainingDatasetSecretKey": "ncp_iam_BPKMKRCFsqyBj1tXD7A2PulPuhDm446h7S"
    }
    
    try:
        # POST 요청 보내기
        response = requests.get(url, headers=headers, json=payload)
        
        # 응답 확인
        print(f"Status Code: {response.status_code}")
        print(f"Response Headers: {dict(response.headers)}")
        
        if response.status_code == 200:
            print("API 호출 성공!")
            print("=" * 50)
            print("응답 내용:")
            print(json.dumps(response.json(), indent=2, ensure_ascii=False))
            print("=" * 50)
        else:
            print(f"API 호출 실패: {response.status_code}")
            print(f"Error Response: {response.text}")
            
    except requests.exceptions.RequestException as e:
        print(f"요청 중 오류 발생: {e}")
    except json.JSONDecodeError as e:
        print(f"JSON 파싱 오류: {e}")

if __name__ == "__main__":
    call_tuning_api()
