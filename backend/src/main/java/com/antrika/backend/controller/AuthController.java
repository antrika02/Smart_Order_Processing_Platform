package com.antrika.backend.controller;

import com.antrika.backend.dto.RegisterRequest;
import com.antrika.backend.entity.User;
import com.antrika.backend.service.AuthService;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/auth")
public class AuthController {

    private final AuthService authService;

    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    @PostMapping("/register")
    @ResponseStatus(HttpStatus.CREATED)
    public User register(@RequestBody RegisterRequest request) {

        return authService.register(
                request.email(),
                request.name(),
                request.password()
        );
    }
}