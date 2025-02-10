package com.example.demo21.service;

import com.example.demo21.dto.LoginRequest;
import com.example.demo21.dto.SignUpRequest;
import com.example.demo21.entity.PublicUserDocument;

import java.util.Optional;

public interface AuthService {
    public String saveUser(String email, String name);
    public String signupUser(SignUpRequest sign);
    PublicUserDocument findByEmail(String email);
    PublicUserDocument registerUser(PublicUserDocument user);
    String forgetPassword(LoginRequest loginRequest);
    void resetPassword(String token, String newPassword);
}
