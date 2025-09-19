resource "aws_db_instance" "database" {
  allocated_storage = 10
  db_name = "qrcode_generator"
  engine = "mysql"
  engine_version = "8.0"
  instance_class = "db.t3.micro"
  username = var.db_username
  password = var.db_password

  # Use the default security group
  # vpc_security_group_ids = [aws_security_group.database_sg.id]
  skip_final_snapshot = true
  multi_az = false
  publicly_accessible = false
}
