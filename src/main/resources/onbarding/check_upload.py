from pymilvus import connections, Collection, utility

connections.connect(host="127.0.0.1", port="19530")
print("컬렉션들:", utility.list_collections())

col = Collection("survey")
col.load()
print("엔티티 수:", col.num_entities)

# ✅ domain을 output_fields에 포함
rows = col.query(
    expr='category == "PRODUCT"',
    output_fields=["id", "category", "domain", "payload"],
    limit=3
)

for r in rows:
    print(r["id"], r["category"], r["domain"], r["payload"]["question"])
