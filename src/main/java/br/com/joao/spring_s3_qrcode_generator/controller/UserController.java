package br.com.joao.spring_s3_qrcode_generator.controller;

import br.com.joao.spring_s3_qrcode_generator.domain.User;
import br.com.joao.spring_s3_qrcode_generator.dto.UserCreateRequest;
import br.com.joao.spring_s3_qrcode_generator.dto.UserResponse;
import br.com.joao.spring_s3_qrcode_generator.service.UserService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.net.URI;

@RestController
@RequestMapping("/api/v1/users")
public class UserController {

    private final UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
    }

    @PostMapping
    public ResponseEntity<UserResponse> createUser(@RequestBody UserCreateRequest req){

        var user = userService.createUser(req);

        return ResponseEntity.created(URI.create("/api/v1/users/" + user.getId())).body(UserResponse.fromDomain(user));

    }

    @DeleteMapping
    public ResponseEntity<Void> disableUser(@AuthenticationPrincipal User logged){

        userService.disableUser(logged);

        return ResponseEntity.noContent().build();

    }

    @PatchMapping("/enable/{id}")
    public ResponseEntity<UserResponse> enableUserById(@PathVariable Long id){

        var user = userService.enableUserById(id);

        return ResponseEntity.ok(UserResponse.fromDomain(user));

    }

    @PatchMapping("/disable/{id}")
    public ResponseEntity<Void> disableUserById(@PathVariable Long id){

        userService.disableUserById(id);

        return ResponseEntity.noContent().build();

    }

    @GetMapping
    public ResponseEntity<Page<UserResponse>> getAllUsers(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size){

        var pageable = PageRequest.of(page, size);

        var users = userService.getAllUsers(pageable);

        return ResponseEntity.ok(users.map(UserResponse::fromDomain));

    }


}
