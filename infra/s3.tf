resource "aws_s3_bucket" "qrcodes_bucket" {
  bucket = var.bucket_name
  tags = {
    Name: "qrcode-bucket"
    Environment: "prod"
  }
}

resource "aws_s3_bucket_ownership_controls" "qrcodes_bucket_ownership" {
  bucket = aws_s3_bucket.qrcodes_bucket.id
  rule {
    object_ownership = "BucketOwnerPreferred"
  }
}

resource "aws_s3_bucket_public_access_block" "qrcodes_not_block" {
  bucket = aws_s3_bucket.qrcodes_bucket.id
  block_public_acls = false
  block_public_policy = false
  ignore_public_acls = false
  restrict_public_buckets = false
}

resource "aws_s3_bucket_policy" "allow_public_read" {
  bucket = aws_s3_bucket.qrcodes_bucket.id
  policy = jsonencode({
    Version = "2012-10-17"
    Statement = [
      {
        Sid = "PublicReadGetObject"
        Effect = "Allow"
        Principal = "*"
        Action = "s3:GetObject"
        Resource = [
          "${aws_s3_bucket.qrcodes_bucket.arn}/*"
        ]
      }
    ]
  })
  depends_on = [aws_s3_bucket_public_access_block.qrcodes_not_block]
}
