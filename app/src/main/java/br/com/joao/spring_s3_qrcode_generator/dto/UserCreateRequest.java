package br.com.joao.spring_s3_qrcode_generator.dto;

public record UserCreateRequest(String fullName, String email, String password, String confirmPassword) {
}
