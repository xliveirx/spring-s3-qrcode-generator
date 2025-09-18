package br.com.joao.spring_s3_qrcode_generator.config;

import br.com.joao.spring_s3_qrcode_generator.service.TokenService;
import br.com.joao.spring_s3_qrcode_generator.service.UserService;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

@Configuration
public class JwtFilterConfig extends OncePerRequestFilter {

    private final TokenService tokenService;
    private final UserService userService;

    public JwtFilterConfig(TokenService tokenService, UserService userService) {
        this.tokenService = tokenService;
        this.userService = userService;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException {

        String token = recoverToken(request);

        if(token != null) {

            var email = tokenService.validateToken(token);
            var user = userService.loadUserByUsername(email);

            var auth = new UsernamePasswordAuthenticationToken(user, null, user.getAuthorities());
            SecurityContextHolder.getContext().setAuthentication(auth);
        }

        filterChain.doFilter(request, response);

    }

    private String recoverToken(HttpServletRequest request) {

        var authorizationHeader = request.getHeader("Authorization");

        if (authorizationHeader != null ) {
            return authorizationHeader.replace("Bearer ", "");
        }

        return null;
    }
}
