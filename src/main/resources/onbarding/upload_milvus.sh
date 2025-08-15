python upload_milvus.py \
  --in survey_embeddings.ndjson \
  --collection survey \
  --host 127.0.0.1 --port 19530 \
  --recreate