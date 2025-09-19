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
      Action = ["s3:GetObject", "s3:PutObject", "s3:deleteObject"],
      Effect = "Allow",
      Resource = [ aws_s3_bucket.qrcodes_bucket.arn, "${aws_s3_bucket.qrcodes_bucket.arn}/*"]
      },
      {
      Action = ["logs:CreateLogGroup","logs:CreateLogStream","logs:PutLogEvents"],
        Effect = "Allow", Resource = "*" }, {
        Action = ["rds:DescribeDBInstances", "rds:Connect"],
        Effect = "Allow",
        Resource = aws_db_instance.database.arn
      }]
  })
}

resource "aws_iam_policy_attachment" "attach" {
  name = "qrcode-generator-lambda-policy-attachment"
  roles = [aws_iam_role.lambda.name]
  policy_arn = aws_iam_policy.lambda.arn
}

resource "aws_lambda_function" "lambda" {
  function_name = "qrcode_generator"
  role = aws_iam_role.lambda.arn
  handler = var.lambda_handler
  runtime = "java17"
  memory_size = 512
  timeout = 30

  filename = "../lambda.zip"
  source_code_hash = filebase64sha256("../lambda.zip")

  environment {
    variables = {
      BUCKET_NAME = aws_s3_bucket.qrcodes_bucket.bucket
    }
  }
}
