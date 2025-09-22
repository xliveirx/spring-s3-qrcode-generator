package br.com.joao.spring_s3_qrcode_generator.service;

import br.com.joao.spring_s3_qrcode_generator.domain.QrCode;
import br.com.joao.spring_s3_qrcode_generator.domain.User;
import br.com.joao.spring_s3_qrcode_generator.dto.QrCodeCreateRequest;
import br.com.joao.spring_s3_qrcode_generator.dto.QrCodeResponse;
import br.com.joao.spring_s3_qrcode_generator.repository.QrCodeRepository;
import br.com.joao.spring_s3_qrcode_generator.exception.NotFoundException;
import br.com.joao.spring_s3_qrcode_generator.exception.ForbiddenException;

import com.google.zxing.BarcodeFormat;
import com.google.zxing.WriterException;
import com.google.zxing.client.j2se.MatrixToImageWriter;
import com.google.zxing.common.BitMatrix;
import com.google.zxing.qrcode.QRCodeWriter;
import jakarta.transaction.Transactional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
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

    private final QrCodeRepository qrCodeRepository;
    private final S3Service s3Service;

    public QrCodeService(QrCodeRepository qrCodeRepository, S3Service s3Service) {
        this.qrCodeRepository = qrCodeRepository;
        this.s3Service = s3Service;
    }

    @Transactional
    public QrCodeResponse createQrCode(QrCodeCreateRequest req, User logged) throws WriterException, IOException {

        QRCodeWriter qrCodeWriter = new QRCodeWriter();

        BitMatrix bitMatrix = qrCodeWriter.encode(req.link(), BarcodeFormat.QR_CODE, 300, 300);

        ByteArrayOutputStream pngOutputStream = new ByteArrayOutputStream();

        MatrixToImageWriter.writeToStream(bitMatrix, "PNG", pngOutputStream);

        byte[] pngQrCodeData = pngOutputStream.toByteArray();

        String key = "user-" + logged.getId() + "/qr-" + UUID.randomUUID() + ".png";

        s3Service.uploadFile(pngQrCodeData, key, "image/png");

        var qrcode = qrCodeRepository.save(new QrCode(req.link(), logged, key));

        var presignedUrl = s3Service.generatePresignedUrl(qrcode.getS3Key(), 10);

        return QrCodeResponse.fromDomain(qrcode, presignedUrl);

    }

    public Page<QrCodeResponse> getAllUserQrCodes(User logged, PageRequest pageable) {

        var qrcodes = qrCodeRepository.findAllByUserId(logged.getId(), pageable);

        return qrcodes
                .map(q -> {
                    var presignedUrl = s3Service.generatePresignedUrl(q.getS3Key(), 10);
                    return QrCodeResponse.fromDomain(q, presignedUrl);
                });
    }

    @Transactional
    public void deleteQrCodeById(Long id, User logged) {

        var qrcode = qrCodeRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("QrCode not found."));

        if(!qrcode.getUser().getId().equals(logged.getId())) {
            throw new ForbiddenException("User is not the owner of this QRCode");
        }

        qrcode.setActive(false);

        try {
            s3Service.deleteFile(qrcode.getS3Key());
        } catch (Exception e) {
            System.err.println("Error deleting S3 object: " + e.getMessage());
        }
    }
}


