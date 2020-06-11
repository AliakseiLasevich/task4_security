package com.example.springsecurity.service;

import com.example.springsecurity.dto.UserRegistrationDto;
import com.example.springsecurity.model.User;
import org.springframework.security.core.userdetails.UserDetailsService;

import java.util.List;

public interface UserService extends UserDetailsService {

    User findByEmail(String email);

    User save(UserRegistrationDto registration);

    List<User> findAll();
}
