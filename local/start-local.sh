docker compose up -d

echo "Waiting for 15 seconds..."
sleep 15
echo "Done waiting."

aws --endpoint-url=http://localhost:4566 rds create-db-instance \
  --db-instance-identifier qrcode-db \
  --db-instance-class db.t2.micro \
  --engine mysql \
  --allocated-storage 20 \
  --master-username admin \
  --master-user-password admin \
  --no-publicly-accessible
