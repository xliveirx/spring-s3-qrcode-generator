resource "aws_db_instance" "database" {
  allocated_storage = 10
  db_name = "qrcode_generator"
  engine = "mysql"
  engine_version = "8.0"
  instance_class = "db.t3.micro"
  username = var.db_username
  password = var.db_password

  vpc_security_group_ids = [aws_security_group.rds.id]
  db_subnet_group_name = aws_db_subnet_group.db_subnets.name

  skip_final_snapshot = true
  multi_az = false
  publicly_accessible = false
}

resource "aws_db_subnet_group" "db_subnets" {
  name       = "qrcode-db-subnets"
  subnet_ids = [aws_subnet.private_a.id, aws_subnet.private_b.id]
}
