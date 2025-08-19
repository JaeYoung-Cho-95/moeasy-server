# -*- coding: utf-8 -*-
import pandas as pd
import os

def sort_c_id_index():
    """
    survey_dataset 01.csv 파일을 로드해서 C_ID 컬럼의 인덱스를 0부터 순차적으로 정렬합니다.
    """
    # CSV 파일 경로
    csv_file = "survey_dataset 01.csv"
    
    # 파일이 존재하는지 확인
    if not os.path.exists(csv_file):
        print(f"오류: {csv_file} 파일을 찾을 수 없습니다.")
        return None
    
    try:
        # CSV 파일 로드 (한국어 인코딩 문제 해결)
        print(f"{csv_file} 파일을 로드 중...")
        df = pd.read_csv(csv_file, encoding='utf-8')
        
        print(f"원본 데이터 형태: {df.shape}")
        print(f"원본 컬럼: {list(df.columns)}")
        
        # C_ID 컬럼이 존재하는지 확인
        if 'C_ID' not in df.columns:
            print("오류: C_ID 컬럼을 찾을 수 없습니다.")
            print(f"사용 가능한 컬럼: {list(df.columns)}")
            return None
        
        print(f"원본 C_ID 값들: {df['C_ID'].head(10).tolist()}")
        
        # C_ID 컬럼의 고유값들을 추출하고 정렬
        unique_c_ids = sorted(df['C_ID'].unique())
        print(f"고유한 C_ID 값들: {unique_c_ids}")
        
        # C_ID를 0부터 시작하는 인덱스로 매핑하는 딕셔너리 생성
        c_id_mapping = {}
        for new_id, old_id in enumerate(unique_c_ids):
            c_id_mapping[old_id] = new_id
        
        print(f"매핑 딕셔너리: {c_id_mapping}")
        
        # 새로운 인덱스로 C_ID 컬럼 업데이트
        df['C_ID'] = df['C_ID'].map(c_id_mapping)
        
        print(f"정렬 완료!")
        print(f"새로운 C_ID 값들 (처음 10개): {df['C_ID'].head(10).tolist()}")
        
        # 결과를 새로운 파일로 저장
        output_file = "survey_dataset_sorted.csv"
        df.to_csv(output_file, index=False, encoding='utf-8')
        print(f"정렬된 데이터가 {output_file}에 저장되었습니다.")
        
        return df
        
    except Exception as e:
        print(f"오류 발생: {e}")
        import traceback
        traceback.print_exc()
        return None

def main():
    """
    메인 함수
    """
    print("C_ID 컬럼 인덱스 정렬 시작...")
    result = sort_c_id_index()
    
    if result is not None:
        print("\n처리 완료!")
        print(f"최종 데이터 형태: {result.shape}")
        print(f"새로운 C_ID 범위: {result['C_ID'].min()} ~ {result['C_ID'].max()}")
        print(f"새로운 C_ID 값들: {sorted(result['C_ID'].unique())}")
    else:
        print("처리 실패!")

if __name__ == "__main__":
    main()
