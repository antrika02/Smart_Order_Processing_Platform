package com.antrika.backend.service;

import com.antrika.backend.auth.PasswordHasher;
import com.antrika.backend.dto.LoginRequest;
import com.antrika.backend.dto.LoginResponse;
import com.antrika.backend.dto.RegisterRequest;
import com.antrika.backend.entity.User;
import com.antrika.backend.exception.InvalidCredentialsException;
import com.antrika.backend.repository.UserRepository;
import org.springframework.stereotype.Service;

@Service
public class AuthService {

    private final UserRepository userRepository;
    private final PasswordHasher passwordHasher;

    public AuthService(
            UserRepository userRepository,
            PasswordHasher passwordHasher
    ) {
        this.userRepository = userRepository;
        this.passwordHasher = passwordHasher;
    }

    public User register(RegisterRequest request) {

        if (userRepository.findByEmail(request.email()).isPresent()) {
            throw new RuntimeException("Email already registered");
        }

        String hashedPassword = passwordHasher.hash(request.password());

        User user = new User();

        user.setName(request.name());
        user.setEmail(request.email());
        user.setPassword(hashedPassword);

        return userRepository.save(user);
    }

    public LoginResponse login(LoginRequest request) {

        User user = userRepository.findByEmail(request.email())
                .orElseThrow(() ->
                        new InvalidCredentialsException("Invalid email or password")
                );

        if (!passwordHasher.matches(
                request.password(),
                user.getPassword()
        )) {
            throw new InvalidCredentialsException("Invalid email or password");
        }

        return new LoginResponse(
                user.getId(),
                user.getName(),
                user.getEmail()
        );
    }
}