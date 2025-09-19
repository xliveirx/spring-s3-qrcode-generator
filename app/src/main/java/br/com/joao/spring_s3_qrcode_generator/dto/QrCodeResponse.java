package br.com.joao.spring_s3_qrcode_generator.dto;

import br.com.joao.spring_s3_qrcode_generator.domain.QrCode;

import java.time.LocalDateTime;

public record QrCodeResponse(Long id, String link, String userEmail, String presignedUrl, Boolean active, LocalDateTime createdAt) {
    public static QrCodeResponse fromDomain(QrCode qrcode, String presignedUrl) {
        return new QrCodeResponse(qrcode.getId(), qrcode.getLink(), qrcode.getUser().getEmail(), presignedUrl, qrcode.getActive(), qrcode.getCreatedAt());
    }
}
