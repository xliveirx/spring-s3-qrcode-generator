sam build

# COMMENT BELOW WHEN DEBUG MODE IS ENABLED
echo "Running..."
sam local start-api \
  --port 3000 \
  --host 0.0.0.0 \
  --docker-network sam-local-net \
  --warm-containers EAGER