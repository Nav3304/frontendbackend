package com.example.demo21.controller;

import com.example.demo21.dto.LoginRequest;
import com.example.demo21.dto.LoginResponse;
import com.example.demo21.dto.SignUpRequest;
import com.example.demo21.entity.PublicUserDocument;
import com.example.demo21.security.CustomUserDetails;
import com.example.demo21.security.JwtUtils;
import com.example.demo21.service.AuthService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@CrossOrigin(origins = "*")
@RequestMapping("/api/auth")
public class AuthController {

    @Autowired
    private AuthService authService;

    @Autowired
    private JwtUtils jwtUtils;

    @Autowired
    private AuthenticationManager authenticationManager;

    @GetMapping("/google-signup")
    public ResponseEntity<String> googleSignUp(@AuthenticationPrincipal OAuth2User principal) {
        if (principal == null) {
            return ResponseEntity.status(401).body("User not authenticated");
        }

        String email = principal.getAttribute("email");
        String name = principal.getAttribute("name");

        String user = authService.saveUser(email, name);
        if ("Email already exist".equals(user)) {
            return ResponseEntity.badRequest().body(user);
        }

        return ResponseEntity.ok().body("User registered successfully: " + user);
    }

    @PostMapping("/sign-in")
    public ResponseEntity<?> authenticateUser(@RequestBody LoginRequest loginRequest) {
        Authentication authentication;
        try {
            authentication = authenticationManager
                    .authenticate(new UsernamePasswordAuthenticationToken(loginRequest.getEmail(), loginRequest.getPassword()));
        } catch (AuthenticationException exception) {
            Map<String, Object> map = new HashMap<>();
            map.put("message", "Bad credentials");
            map.put("status", false);
            return new ResponseEntity<Object>(map, HttpStatus.NOT_FOUND);
        }

        SecurityContextHolder.getContext().setAuthentication(authentication);
        CustomUserDetails customUserDetails = (CustomUserDetails) authentication.getPrincipal();

        String name = customUserDetails.getName();
        String jwtToken = jwtUtils.generateTokenFromUsername(customUserDetails);
        LoginResponse response = new LoginResponse(name, jwtToken);

        return ResponseEntity.ok(response);
    }
    @PostMapping("/sign-up")
    public ResponseEntity<String> signUp(@RequestBody SignUpRequest signUpRequest){
        String response=authService.signupUser(signUpRequest);
        return ResponseEntity.ok().body(response);
    }
    @PostMapping("/forgot-password")
    public ResponseEntity<String> forgotPassword(@RequestBody LoginRequest loginRequest){
        String response=authService.forgetPassword(loginRequest);
        return ResponseEntity.ok().body(response);
    }
    @PostMapping("/reset-password")
    public ResponseEntity<?> resetPassword(@RequestParam String token, @RequestParam String newPassword) {
        authService.resetPassword(token, newPassword);
        return ResponseEntity.ok("Password reset successfully.");
    }
}