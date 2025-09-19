package br.com.joao.spring_s3_qrcode_generator.service;

import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.model.GeneratePresignedUrlRequest;
import com.amazonaws.services.s3.model.ObjectMetadata;
import com.amazonaws.services.s3.model.PutObjectRequest;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.net.URL;
import java.util.Date;

@Service
public class S3Service {

    @Value("${aws.s3.bucket-name}")
    private String bucketName;

    private final AmazonS3 amazonS3;

    public S3Service(AmazonS3 amazonS3) {
        this.amazonS3 = amazonS3;
    }

    public String generatePresignedUrl(String key, int expirationInMinutes) {
        Date expiration = new Date(System.currentTimeMillis() + expirationInMinutes * 60 * 1000);

        GeneratePresignedUrlRequest generatePresignedUrlRequest =
                new GeneratePresignedUrlRequest(bucketName, key)
                        .withMethod(com.amazonaws.HttpMethod.GET)
                        .withExpiration(expiration);

        URL url = amazonS3.generatePresignedUrl(generatePresignedUrlRequest);
        return url.toString();
    }

    public void uploadFile(byte[] fileData, String fileName, String contentType) throws IOException {
        ObjectMetadata objectMetadata = new ObjectMetadata();

        objectMetadata.setContentType(contentType);
        objectMetadata.setContentLength(fileData.length);

        try (ByteArrayInputStream inputStream = new ByteArrayInputStream(fileData)) {
            PutObjectRequest putObjectRequest = new PutObjectRequest(bucketName, fileName, inputStream, objectMetadata);
            amazonS3.putObject(putObjectRequest);

        } catch (IOException e) {
            throw new RuntimeException("Erro ao fazer upload do arquivo ao S3.", e);
        }
    }
}
