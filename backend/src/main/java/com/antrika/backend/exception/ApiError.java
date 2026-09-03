package com.antrika.backend.exception;

public record ApiError(
        int status,
        String message
) {}