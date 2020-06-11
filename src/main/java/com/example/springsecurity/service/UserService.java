package com.example.springsecurity.service;

import com.example.springsecurity.dto.UserRegistrationDto;
import com.example.springsecurity.model.User;
import org.springframework.security.core.userdetails.UserDetailsService;

public interface UserService extends UserDetailsService {

    User findByEmail(String email);

    User save(UserRegistrationDto registration);
}
