package com.antrika.backend.dto;

public record LoginResponse(
        Long id,
        String name,
        String email,
        String token
) {
}