package com.example.demo21.service.Implementation;

import com.example.demo21.dto.LoginRequest;
import com.example.demo21.dto.SignUpRequest;
import com.example.demo21.entity.PublicUserDocument;
import com.example.demo21.repository.PublicUserRepository;
import com.example.demo21.service.AuthService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.crypto.bcrypt.BCrypt;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserService;
import org.springframework.security.oauth2.core.OAuth2AccessToken;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.user.DefaultOAuth2User;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;

import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
public class AuthServiceImpl implements AuthService, OAuth2UserService<OAuth2UserRequest, OAuth2User> {

    @Autowired
    private PublicUserRepository publicUserRepository;

    @Autowired
    private JavaMailSender mailSender;

    @Autowired
    private ContactServiceImpl contactService;
    @Value("${reset_url}")
    private String passwordResetUrl;

    private final WebClient webClient = WebClient.builder().build();


    @Override
    public String saveUser (String email, String name) {
        PublicUserDocument existingUser = publicUserRepository.findByEmail(email);
        if (existingUser!=null) {
            return "Email already exist"; // Return existing user
        }
        PublicUserDocument user = new PublicUserDocument();
        user.setEmail(email);
        user.setName(name);
        publicUserRepository.save(user);
        return "Login successfully";
    }

    @Override
    public String signupUser(SignUpRequest sign) {
        // Validate required fields
        if (sign.getEmail() == null || sign.getEmail().isEmpty()) {
            throw new IllegalArgumentException("Email is required");
        }
        if (sign.getName() == null || sign.getName().isEmpty()) {
            throw new IllegalArgumentException("Name is required");
        }
        if (sign.getPassword() == null || sign.getPassword().isEmpty()) {
            throw new IllegalArgumentException("Password is required");
        }

        // Create a new user document
        PublicUserDocument publicUserDocument = new PublicUserDocument();
        publicUserDocument.setEmail(sign.getEmail());
        publicUserDocument.setName(sign.getName());

        // Set optional fields
        Optional.ofNullable(sign.getAddress()).ifPresent(publicUserDocument::setAddress);
        Optional.ofNullable(sign.getPhoneNumber()).ifPresent(publicUserDocument::setPhoneNumber);
        Optional.ofNullable(sign.getNearestStore()).ifPresent(publicUserDocument::setNearestStore);

        // Encrypt and set the password
        BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();
        String encryptedPassword = passwordEncoder.encode(sign.getPassword());
        publicUserDocument.setPassword(encryptedPassword);

        publicUserRepository.save(publicUserDocument);

        return "User sign up successfully";
    }

    @Override
    public PublicUserDocument findByEmail (String email) {
        return publicUserRepository.findByEmail(email);
    }

    @Override
    public PublicUserDocument registerUser (PublicUserDocument user) {
        if (user.getPassword() != null)
            user.setPassword(new BCryptPasswordEncoder().encode(user.getPassword()));
        return publicUserRepository.save(user);
    }

//    @Override
//    public String forgetPassword (LoginRequest loginRequest) {
//        PublicUserDocument user = publicUserRepository.findByEmail(loginRequest.getEmail());
//        if (user==null) {
//            return "User not found";
//        }
//
//        user.setPassword(new BCryptPasswordEncoder().encode(loginRequest.getPassword()));
//        publicUserRepository.save(user);
//
//        return "Password updated successfully.";
//    }
    @Override
    public String forgetPassword(LoginRequest loginRequest) {
        PublicUserDocument user = publicUserRepository.findByEmail(loginRequest.getEmail());
        if (user != null) {
            String resetToken = UUID.randomUUID().toString();
            user.setResetToken(resetToken);
            publicUserRepository.save(user);

            String resetLink =passwordResetUrl + "/reset-password?token=" + resetToken;
            contactService.sendEmail(loginRequest.getEmail(), "Reset Password", resetLink);
        }
        return "Password updated successfully.";
    }

    public void resetPassword(String token, String newPassword) {
        PublicUserDocument user = publicUserRepository.findByResetToken(token);
        if (user != null) {
            user.setPassword(new BCryptPasswordEncoder().encode(newPassword)); // In a real application, you should hash the password
            user.setResetToken(null);
            publicUserRepository.save(user);
        }
    }


    @Override
    public OAuth2User loadUser(OAuth2UserRequest userRequest) throws OAuth2AuthenticationException {
        String userInfoUrl = userRequest.getClientRegistration()
                .getProviderDetails()
                .getUserInfoEndpoint()
                .getUri();

        Map userInfo = webClient
                .get()
                .uri(userInfoUrl)
                .headers(headers -> headers.setBearerAuth(userRequest.getAccessToken().getTokenValue()))
                .retrieve()
                .bodyToMono(Map.class)
                .block();

        String email = (String) userInfo.get("email");
        String name = (String) userInfo.get("name");

        PublicUserDocument existingUser = publicUserRepository.findByEmail(email);

        if (existingUser == null) {
            PublicUserDocument newUser = new PublicUserDocument();
            newUser.setEmail(email);
            newUser.setName(name);
            publicUserRepository.save(newUser);
        }

        // Derive authorities from the access token scopes
        Set<SimpleGrantedAuthority> authorities = userRequest.getAccessToken().getScopes()
                .stream()
                .map(scope -> new SimpleGrantedAuthority("SCOPE_" + scope))
                .collect(Collectors.toSet());

        return new DefaultOAuth2User(authorities, userInfo, "name"); // Change "name" to the attribute for username // Change "name" to the attribute that holds the username in the OAuth2 provider response
    }
}
