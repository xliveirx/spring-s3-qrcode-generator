package br.com.joao.spring_s3_qrcode_generator.repository;

import br.com.joao.spring_s3_qrcode_generator.domain.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Long> {
    Optional<User> findByEmailIgnoreCaseAndActiveTrue(String username);
}
