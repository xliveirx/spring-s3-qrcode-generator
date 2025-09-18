package br.com.joao.spring_s3_qrcode_generator.service;

import br.com.joao.spring_s3_qrcode_generator.domain.User;
import br.com.joao.spring_s3_qrcode_generator.dto.UserCreateRequest;
import br.com.joao.spring_s3_qrcode_generator.repository.UserRepository;
import jakarta.transaction.Transactional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service
public class UserService implements UserDetailsService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public UserService(UserRepository userRepository, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        return userRepository.findByEmailIgnoreCase(username)
                .orElseThrow(() -> new UsernameNotFoundException("User not found."));
    }

    @Transactional
    public User createUser(UserCreateRequest req) {

        if(userRepository.findByEmailIgnoreCase(req.email()).isPresent()){
            throw new RuntimeException("Email already registered");
        }

        if(!req.password().equals(req.confirmPassword())){
            throw new RuntimeException("Passwords don't match");
        }

        var encodedPassword = passwordEncoder.encode(req.password());

        var user = new User(req.fullName(), req.email(), encodedPassword);

        return userRepository.save(user);
    }

    @Transactional
    public void disableUser(User logged) {

        var user = userRepository.findById(logged.getId())
                .orElseThrow(() -> new RuntimeException("User not found."));

        user.setActive(false);
        user.setUpdatedAt(LocalDateTime.now());

        userRepository.save(user);

    }

    @Transactional
    public User enableUserById(Long id) {

        var user = userRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("User not found."));

        user.setActive(true);
        user.setUpdatedAt(LocalDateTime.now());

        return userRepository.save(user);

    }

    @Transactional
    public void disableUserById(Long id) {

        var user = userRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("User not found."));

        user.setActive(false);
        user.setUpdatedAt(LocalDateTime.now());

        userRepository.save(user);

    }

    public Page<User> getAllUsers(PageRequest pageable) {

        return userRepository.findAll(pageable);
    }
}
