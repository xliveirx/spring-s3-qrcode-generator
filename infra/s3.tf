resource "aws_s3_bucket" "qrcodes_bucket" {
  bucket = var.bucket_name
  tags = {
    Name: aws_s3_bucket.qrcodes_bucket.bucket
    Environment: "prod"
  }
}

resource "aws_s3_bucket_public_access_block" "qrcodes_not_block" {
  bucket = aws_s3_bucket.qrcodes_bucket.id
  block_public_acls = false
  block_public_policy = false
  ignore_public_acls = false
  restrict_public_buckets = false
}

resource "aws_s3_bucket_acl" "qrcodes_allow_public-read" {
  bucket = aws_s3_bucket.qrcodes_bucket.id
  acl = "public-read"
}