package com.example.demo21.security;

import lombok.Data;

@Data
public class JwtValidationResult {
    private boolean valid;
    private String message;

    // Constructors, getters, and setters
    public JwtValidationResult(boolean valid, String message) {
        this.valid = valid;
        this.message = message;
    }
}

