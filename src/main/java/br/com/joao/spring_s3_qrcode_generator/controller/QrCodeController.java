package br.com.joao.spring_s3_qrcode_generator.controller;

import br.com.joao.spring_s3_qrcode_generator.domain.User;
import br.com.joao.spring_s3_qrcode_generator.dto.QrCodeCreateRequest;
import br.com.joao.spring_s3_qrcode_generator.dto.QrCodeResponse;
import br.com.joao.spring_s3_qrcode_generator.service.QrCodeService;
import com.google.zxing.WriterException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.net.URI;

@RestController
@RequestMapping("/api/v1/qrcodes")
public class QrCodeController {

    private final QrCodeService qrCodeService;

    public QrCodeController(QrCodeService qrCodeService) {
        this.qrCodeService = qrCodeService;
    }

    @PostMapping
    public ResponseEntity<QrCodeResponse> createQrCode(@RequestBody QrCodeCreateRequest req, @AuthenticationPrincipal User logged) throws IOException, WriterException {

        var qrcode = qrCodeService.createQrCode(req, logged);

        return ResponseEntity.created(URI.create(qrcode.presignedUrl())).body(qrcode);
    }

    @GetMapping
    public ResponseEntity<Page<QrCodeResponse>> getAllUserQrCodes(@RequestParam(defaultValue = "0") int page,
                                                                  @RequestParam(defaultValue = "10") int size,
                                                                  @AuthenticationPrincipal User logged){

        var pageable = PageRequest.of(page, size);

        var qrcodes = qrCodeService.getAllUserQrCodes(logged, pageable);

        return ResponseEntity.ok(qrcodes);

    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteQrCodeById(@PathVariable Long id,
                                                 @AuthenticationPrincipal User logged){

        qrCodeService.deleteQrCodeById(id, logged);

        return ResponseEntity.noContent().build();
    }

}
