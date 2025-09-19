resource "aws_db_instance" "database" {
  allocated_storage = 10
  db_name = "qrcode_generator"
  engine = "mysql"
  engine_version = "8.0"
  instance_class = "db.t3.micro"
  username = var.db_username
  password = var.db_password

  vpc_security_group_ids = [aws_security_group.database_sg.id]
  multi_az = false publicly_accessible = false
}

resource "aws_security_group" "database_sg" {
  name = "database_sg"
  description = "Security group for RDS database"

  ingress {
    from_port = 3306
    to_port = 3306
    protocol = "tcp"
    cidr_blocks = ["0.0.0.0/0"]
  }

  egress {
    from_port = 0
    to_port = 0
    protocol = "-1"
    cidr_blocks = ["0.0.0.0/0"]
  }
}