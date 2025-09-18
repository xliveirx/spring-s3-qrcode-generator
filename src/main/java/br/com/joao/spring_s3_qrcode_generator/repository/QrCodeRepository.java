package br.com.joao.spring_s3_qrcode_generator.repository;

import br.com.joao.spring_s3_qrcode_generator.domain.QrCode;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface QrCodeRepository extends JpaRepository<QrCode, Long> {
    Page<QrCode> findAllByUserId(Long userId, Pageable pageable);
}
