package com.antrika.backend.service;

import com.antrika.backend.auth.PasswordHasher;
import com.antrika.backend.dto.LoginRequest;
import com.antrika.backend.dto.LoginResponse;
import com.antrika.backend.dto.RegisterRequest;
import com.antrika.backend.dto.UserResponse;
import com.antrika.backend.entity.User;
import com.antrika.backend.exception.InvalidCredentialsException;
import com.antrika.backend.exception.UserAlreadyExistsException;
import com.antrika.backend.repository.UserRepository;
import com.antrika.backend.security.JwtService;
import org.springframework.stereotype.Service;

@Service
public class AuthService {

    private final UserRepository userRepository;
    private final PasswordHasher passwordHasher;
    private final JwtService jwtService;

    public AuthService(
            UserRepository userRepository,
            PasswordHasher passwordHasher,
            JwtService jwtService
    ) {
        this.userRepository = userRepository;
        this.passwordHasher = passwordHasher;
        this.jwtService = jwtService;
    }

    public UserResponse register(RegisterRequest request) {

        if (userRepository.findByEmail(request.email()).isPresent()) {
            throw new UserAlreadyExistsException("Email already registered");
        }

        String hashedPassword = passwordHasher.hash(request.password());

        User user = new User();

        user.setName(request.name());
        user.setEmail(request.email());
        user.setPassword(hashedPassword);

        User savedUser = userRepository.save(user);

        return new UserResponse(
                savedUser.getId(),
                savedUser.getName(),
                savedUser.getEmail()
        );
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

        String token = jwtService.generateToken(
                user.getId(),
                user.getEmail()
        );

        return new LoginResponse(
                user.getId(),
                user.getName(),
                user.getEmail(),
                token
        );
    }
}