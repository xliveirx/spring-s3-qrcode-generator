resource "aws_iam_role" "lambda" {
  name = "qrcode-generator-lambda-role"
  assume_role_policy = jsonencode({
    Version = "2012-10-17"
    Statement = [{
      Action = "sts:AssumeRole",
      Effect = "Allow",
      Principal = { Service = "lambda.amazonaws.com" }
    }]
  })
}

resource "aws_iam_policy" "lambda" {
  name = "qrcode-generator-lambda-policy"
  policy = jsonencode({
    Version = "2012-10-17"
    Statement = [
      {
      Action = ["s3:GetObject", "s3:PutObject", "s3:DeleteObject"],
      Effect = "Allow",
      Resource = [ aws_s3_bucket.qrcodes_bucket.arn, "${aws_s3_bucket.qrcodes_bucket.arn}/*"]
      },
      {
      Action = ["logs:CreateLogGroup","logs:CreateLogStream","logs:PutLogEvents"],
        Effect = "Allow", Resource = "*" }, {
        Action = ["rds:DescribeDBInstances"],
        Effect = "Allow",
        Resource = aws_db_instance.database.arn
      },
      {
        Effect = "Allow",
        Action = [
          "ec2:CreateNetworkInterface",
          "ec2:DescribeNetworkInterfaces",
          "ec2:DeleteNetworkInterface",
          "ec2:AssignPrivateIpAddresses",
          "ec2:UnassignPrivateIpAddresses"
        ],
        Resource = "*"
      }
    ]
  })
}

resource "aws_iam_policy_attachment" "attach" {
  name = "qrcode-generator-lambda-policy-attachment"
  roles = [aws_iam_role.lambda.name]
  policy_arn = aws_iam_policy.lambda.arn
}

resource "aws_s3_object" "lambda_package" {
  bucket = var.lambda_bucket_name
  key    = "lambda/prod/lambda.jar"
  source = "${path.module}/../app/target/spring-s3-qrcode-generator-0.0.1-SNAPSHOT.jar"
  # etag   = filemd5("${path.module}/../app/target/spring-s3-qrcode-generator-0.0.1-SNAPSHOT.jar")
}

resource "aws_lambda_function" "lambda" {
  function_name = "qrcode_generator"
  role          = aws_iam_role.lambda.arn
  handler       = var.lambda_handler
  runtime       = "java17"
  timeout       = 30
  memory_size   = 1024

  # em vez de filename:
  s3_bucket = aws_s3_object.lambda_package.bucket
  s3_key    = aws_s3_object.lambda_package.key

  depends_on = [aws_s3_object.lambda_package]

 vpc_config {
   security_group_ids = [aws_security_group.lambda.id]
   subnet_ids = [aws_subnet.private_a.id, aws_subnet.private_b.id]
 }
}