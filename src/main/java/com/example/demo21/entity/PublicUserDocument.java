package com.example.demo21.entity;

import lombok.Data;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collection;
import java.util.List;

@Data
@Document(collection = "PublicUser")
public class PublicUserDocument implements UserDetails {
    private String id;
    private String email;
    private String name;
    private String password;
    private String phoneNumber;
    private String address;
    private String nearestStore;
    private String signUpMethod;
    private String resetToken;

    public PublicUserDocument (String name,String email) {
        this.name=name;
        this.email=email;
    }
    public PublicUserDocument(){}

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities () {
        return List.of();
    }

    @Override
    public String getUsername () {
        return "";
    }
}
