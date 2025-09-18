package br.com.joao.spring_s3_qrcode_generator.service;

import br.com.joao.spring_s3_qrcode_generator.domain.User;
import com.auth0.jwt.JWT;
import com.auth0.jwt.JWTVerifier;
import com.auth0.jwt.algorithms.Algorithm;
import com.auth0.jwt.exceptions.JWTCreationException;
import com.auth0.jwt.exceptions.JWTVerificationException;
import com.auth0.jwt.interfaces.DecodedJWT;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneOffset;

@Service
public class TokenService {

    public String generateToken(User user){

        Algorithm algorithm = Algorithm.HMAC256("supersecret");

        try {
            return JWT.create()
                    .withIssuer("api")
                    .withSubject(user.getUsername())
                    .withExpiresAt(expiresAt(30))
                    .sign(algorithm);
        } catch (JWTCreationException exception) {

            throw new RuntimeException("Erro ao gerar token JWT", exception);
        }
    }

    public String validateToken(String token) {

        DecodedJWT decodedJWT;

        try {
            Algorithm algorithm = Algorithm.HMAC256("supersecret");
            JWTVerifier verifier = JWT.require(algorithm)
                    .withIssuer("api")
                    .build();

            decodedJWT = verifier.verify(token);
            return decodedJWT.getSubject();

        } catch (JWTVerificationException exception) {
            throw new RuntimeException("Erro ao validar token JWT", exception);
        }
    }

    private Instant expiresAt(int minutes) {
        return LocalDateTime.now().plusMinutes(minutes).toInstant(ZoneOffset.of("-03:00"));
    }
}
