package com.example.demo21.controller;

import com.example.demo21.dto.PublicUserRequest;
import com.example.demo21.dto.PublicUserResponse;
import com.example.demo21.security.JwtUtils;
import com.example.demo21.security.JwtValidationResult;
import com.example.demo21.service.PublicUserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/user")
@CrossOrigin(origins = "*")
public class PublicUserController {

    @Autowired
    private PublicUserService publicUserService;

    @Autowired
    private JwtUtils jwtTokenProvider; // Assuming you have a JwtTokenProvider for token validation

    @GetMapping("")
    public ResponseEntity<?> getUserByEmail(@RequestHeader("Authorization") String token) {
        // Remove "Bearer " prefix from the token if present
        if (token.startsWith("Bearer ")) {
            token = token.substring(7);
        }

        // Validate the token and handle errors
        JwtValidationResult result = jwtTokenProvider.validateJwtToken(token);
        if (!result.isValid()) {
            // Return an appropriate error response
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(Map.of("error", result.getMessage()));
        }

        // Extract email from the token
        String email = jwtTokenProvider.getEmailFromToken(token);

        // Fetch the user by email
        PublicUserResponse user = publicUserService.getUserByEmail(email);

        // Return the user details in the response
        return ResponseEntity.ok(user);
    }


    // Endpoint to edit user details
    @PutMapping("/edit")
    public ResponseEntity<PublicUserResponse> editUser(@RequestBody PublicUserRequest publicUserRequest) {
        // Edit user details and return updated user
        PublicUserResponse updatedUser = publicUserService.editUser(publicUserRequest);
        return ResponseEntity.ok(updatedUser);
    }

}
