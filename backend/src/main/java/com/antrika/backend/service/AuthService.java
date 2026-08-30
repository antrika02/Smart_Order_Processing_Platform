package com.antrika.backend.service;

import com.antrika.backend.auth.PasswordHasher;
import com.antrika.backend.entity.User;
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

    public User register(String email, String name, String password) {

        User user = new User();

        user.setEmail(email);
        user.setName(name);
        user.setPassword(passwordHasher.hash(password));

        return userRepository.save(user);
    }
}