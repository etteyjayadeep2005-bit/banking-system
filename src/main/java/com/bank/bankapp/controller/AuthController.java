package com.bank.bankapp.controller;

import com.bank.bankapp.dto.LoginRequest;
import com.bank.bankapp.dto.RegisterRequest;
import com.bank.bankapp.exception.BadRequestException;
import com.bank.bankapp.exception.UnauthorizedException;
import com.bank.bankapp.model.AppUser;
import com.bank.bankapp.repository.UserRepository;
import com.bank.bankapp.security.TokenService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final UserRepository users;
    private final PasswordEncoder encoder;
    private final TokenService tokens;

    @Value("${app.admin.username}")
    private String adminUsername;

    public AuthController(UserRepository users, PasswordEncoder encoder, TokenService tokens) {
        this.users = users;
        this.encoder = encoder;
        this.tokens = tokens;
    }

    @PostMapping("/register")
    @ResponseStatus(HttpStatus.CREATED)
    public Map<String, String> register(@Valid @RequestBody RegisterRequest req) {
        String username = req.username().trim().toLowerCase();
        if (username.equals(adminUsername.trim().toLowerCase()) || users.existsByUsername(username)) {
            throw new BadRequestException("Username already taken");
        }
        AppUser user = new AppUser();
        user.setUsername(username);
        user.setPasswordHash(encoder.encode(req.password()));
        user.setRole("CUSTOMER");
        users.save(user);
        return Map.of("message", "Registered successfully", "username", username);
    }

    @PostMapping("/login")
    public Map<String, String> login(@Valid @RequestBody LoginRequest req) {
        String username = req.username().trim().toLowerCase();
        AppUser user = users.findByUsername(username)
                .filter(u -> encoder.matches(req.password(), u.getPasswordHash()))
                .orElseThrow(() -> new UnauthorizedException("Invalid username or password"));
        return Map.of(
                "token", tokens.generate(user),
                "tokenType", "Bearer",
                "role", user.getRole(),
                "username", username);
    }
}