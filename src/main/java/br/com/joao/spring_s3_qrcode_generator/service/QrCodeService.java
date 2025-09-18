package br.com.joao.spring_s3_qrcode_generator.service;

import br.com.joao.spring_s3_qrcode_generator.domain.QrCode;
import br.com.joao.spring_s3_qrcode_generator.domain.User;
import br.com.joao.spring_s3_qrcode_generator.dto.QrCodeCreateRequest;
import br.com.joao.spring_s3_qrcode_generator.dto.QrCodeResponse;
import br.com.joao.spring_s3_qrcode_generator.repository.QrCodeRepository;

import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.model.GeneratePresignedUrlRequest;
import com.amazonaws.services.s3.model.ObjectMetadata;
import com.amazonaws.services.s3.model.PutObjectRequest;
import com.google.zxing.BarcodeFormat;
import com.google.zxing.WriterException;
import com.google.zxing.client.j2se.MatrixToImageWriter;
import com.google.zxing.common.BitMatrix;
import com.google.zxing.qrcode.QRCodeWriter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.net.URL;
import java.util.Date;
import java.util.UUID;

@Service
public class QrCodeService {


    @Value("${aws.s3.bucket-name}")
    private String bucketName;

    private final QrCodeRepository qrCodeRepository;
    private final AmazonS3 amazonS3;

    public QrCodeService(QrCodeRepository qrCodeRepository, AmazonS3 amazonS3) {
        this.qrCodeRepository = qrCodeRepository;
        this.amazonS3 = amazonS3;
    }

    public QrCodeResponse createQrCode(QrCodeCreateRequest req, User logged) throws WriterException, IOException {

        QRCodeWriter qrCodeWriter = new QRCodeWriter();

        BitMatrix bitMatrix = qrCodeWriter.encode(req.link(), BarcodeFormat.QR_CODE, 300, 300);

        ByteArrayOutputStream pngOutputStream = new ByteArrayOutputStream();

        MatrixToImageWriter.writeToStream(bitMatrix, "PNG", pngOutputStream);

        byte[] pngQrCodeData = pngOutputStream.toByteArray();

        String key = "user-" + logged.getId() + "/qr-" + UUID.randomUUID() + ".png";

        uploadFile(pngQrCodeData, key, "image/png");

        var qrcode = qrCodeRepository.save(new QrCode(req.link(), logged, key));

        var presignedUrl = generatePresignedUrl(qrcode.getS3Key(), 10);

        return QrCodeResponse.fromDomain(qrcode, presignedUrl);

    }

    public Page<QrCodeResponse> getAllUserQrCodes(User logged, PageRequest pageable) {

        var qrcodes = qrCodeRepository.findAllByUserId(logged.getId(), pageable);

        return qrcodes
                .map(q -> {
                    var presignedUrl = generatePresignedUrl(q.getS3Key(), 10);
                    return QrCodeResponse.fromDomain(q, presignedUrl);
                });
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

    public void deleteQrCodeById(Long id, User logged) {

        var qrcode = qrCodeRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("QrCode not found."));

        if (qrcode.getUser().getId().equals(logged.getId())) {
            qrcode.setActive(false);
            qrCodeRepository.save(qrcode);

            amazonS3.deleteObject(bucketName, qrcode.getS3Key());
        }
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
}
