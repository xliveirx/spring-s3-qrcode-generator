package br.com.joao.spring_s3_qrcode_generator.controller;

import br.com.joao.spring_s3_qrcode_generator.domain.User;
import br.com.joao.spring_s3_qrcode_generator.dto.LoginRequest;
import br.com.joao.spring_s3_qrcode_generator.dto.LoginResponse;
import br.com.joao.spring_s3_qrcode_generator.service.TokenService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/login")
public class LoginController {

    private final TokenService tokenService;
    private final AuthenticationManager authenticationManager;

    public LoginController(TokenService tokenService, AuthenticationManager authenticationManager) {
        this.tokenService = tokenService;
        this.authenticationManager = authenticationManager;
    }

    @PostMapping
    public ResponseEntity<LoginResponse> login(@RequestBody LoginRequest req) {

        var authenticationToken = new UsernamePasswordAuthenticationToken(req.email(), req.password());

        var authentication = authenticationManager.authenticate(authenticationToken);

        String token = tokenService.generateToken((User)authentication.getPrincipal());

        return ResponseEntity.ok(new LoginResponse(token));
    }
}
