package br.com.joao.spring_s3_qrcode_generator.dto;

import br.com.joao.spring_s3_qrcode_generator.domain.Role;
import br.com.joao.spring_s3_qrcode_generator.domain.User;

import java.time.LocalDateTime;
import java.util.UUID;

public record UserResponse(Long id, String fullName, String email, Role role, LocalDateTime createdAt, LocalDateTime updatedAt) {
        public static UserResponse fromDomain(User user) {
            return new UserResponse(user.getId(), user.getFullName(), user.getEmail(), user.getRole(), user.getCreatedAt(), user.getUpdatedAt());
        }
}
