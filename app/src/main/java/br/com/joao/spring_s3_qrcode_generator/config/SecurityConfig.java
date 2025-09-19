package br.com.joao.spring_s3_qrcode_generator.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    private final JwtFilterConfig jwtFilterConfig;

    public SecurityConfig(JwtFilterConfig jwtFilterConfig) {
        this.jwtFilterConfig = jwtFilterConfig;
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public AuthenticationManager authenticationManagerBean(AuthenticationConfiguration authenticationConfiguration) throws Exception {
        return authenticationConfiguration.getAuthenticationManager();
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {

        return http
                .authorizeHttpRequests(req -> {
                    req.requestMatchers(HttpMethod.POST, "/api/v1/login").permitAll();
                    req.requestMatchers(HttpMethod.POST, "/api/v1/users").permitAll();

                    req.requestMatchers(HttpMethod.GET, "/api/v1/users").hasRole("ADMIN");
                    req.requestMatchers(HttpMethod.DELETE, "/api/v1/users/disable/{id}").hasRole("ADMIN");
                    req.requestMatchers(HttpMethod.PATCH, "/api/v1/users/enable/{id}").hasRole("ADMIN");

                    req.anyRequest().authenticated();

                })
                .addFilterBefore(jwtFilterConfig, UsernamePasswordAuthenticationFilter.class)
                .sessionManagement(sm -> sm.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .csrf(AbstractHttpConfigurer::disable)
                .build();

    }
}
