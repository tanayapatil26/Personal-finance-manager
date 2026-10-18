package com.finance.controller;

import com.finance.dto.AuthRequest;
import com.finance.dto.AuthResponse;
import com.finance.model.AppUser;
import com.finance.repository.UserRepository;
import com.finance.security.JwtService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final UserRepository users;
    private final PasswordEncoder encoder;
    private final JwtService jwt;

    public AuthController(UserRepository users, PasswordEncoder encoder, JwtService jwt) {
        this.users = users;
        this.encoder = encoder;
        this.jwt = jwt;
    }

    @PostMapping("/register")
    @ResponseStatus(HttpStatus.CREATED)
    public AuthResponse register(@Valid @RequestBody AuthRequest req) {
        String username = req.username().trim().toLowerCase();
        if (users.existsByUsername(username)) {
            throw new IllegalStateException("That username is taken. Choose another one.");
        }
        AppUser user = users.save(new AppUser(username, encoder.encode(req.password())));
        return new AuthResponse(jwt.generate(user.getId(), user.getUsername()), user.getUsername());
    }

    @PostMapping("/login")
    public AuthResponse login(@Valid @RequestBody AuthRequest req) {
        AppUser user = users.findByUsername(req.username().trim().toLowerCase())
                .filter(u -> encoder.matches(req.password(), u.getPasswordHash()))
                .orElseThrow(() -> new BadCredentialsException("Invalid username or password"));
        return new AuthResponse(jwt.generate(user.getId(), user.getUsername()), user.getUsername());
    }
}
