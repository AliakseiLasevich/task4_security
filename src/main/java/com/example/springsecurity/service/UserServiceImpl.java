package com.example.springsecurity.service;

import java.time.LocalDate;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

import com.example.springsecurity.dto.UserRegistrationDto;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationListener;
import org.springframework.security.authentication.event.AuthenticationSuccessEvent;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

import com.example.springsecurity.model.Role;
import com.example.springsecurity.model.User;
import com.example.springsecurity.repository.UserRepository;

@Service
public class UserServiceImpl implements UserService, ApplicationListener<AuthenticationSuccessEvent> {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private BCryptPasswordEncoder passwordEncoder;

    public User findByEmail(String email) {
        return userRepository.findByEmail(email);
    }

    public User save(UserRegistrationDto registration) {
        User user = new User();
        user.setRegisterDate(LocalDate.now());
        user.setFirstName(registration.getFirstName());
        user.setLastName(registration.getLastName());
        user.setEmail(registration.getEmail());
        user.setPassword(passwordEncoder.encode(registration.getPassword()));
        user.setRoles(Arrays.asList(new Role("ROLE_USER")));
        return userRepository.save(user);
    }

    @Override
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
        User user = userRepository.findByEmail(email);
        if (user == null) {
            throw new UsernameNotFoundException("Invalid username or password.");
        }
        return new org.springframework.security.core.userdetails.User(user.getEmail(),
                user.getPassword(),
                mapRolesToAuthorities(user.getRoles()));
    }

    private Collection<? extends GrantedAuthority> mapRolesToAuthorities(Collection<Role> roles) {
        return roles.stream()
                .map(role -> new SimpleGrantedAuthority(role.getName()))
                .collect(Collectors.toList());
    }

    @Override
    public void onApplicationEvent(AuthenticationSuccessEvent event) {
        String userEmail = ((UserDetails) event.getAuthentication().
                getPrincipal()).getUsername();
        User user = userRepository.findByEmail(userEmail);
        user.setLastLoginDate(LocalDate.now());
        userRepository.save(user);
    }

    @Override
    public List<User> findAll() {
        return userRepository.findAll();
    }

    @Override
    public void block(List<Integer> users) {
        users.stream()
                .map(userId -> userRepository.findById((long) userId).orElseThrow(RuntimeException::new))
                .peek(user -> user.setActive(false))
                .forEach(user -> userRepository.save(user));
    }

    @Override
    public void unblock(List<Integer> users) {
        users.stream()
                .map(userId -> userRepository.findById((long) userId).orElseThrow(RuntimeException::new))
                .peek(user -> user.setActive(true))
                .forEach(user -> userRepository.save(user));
    }

    @Override
    public void delete(List<Integer> users) {
        users.stream()
                .map(userId -> userRepository.findById((long) userId).orElseThrow(RuntimeException::new))
                .peek(user -> user.setActive(true))
                .forEach(user -> userRepository.delete(user));
    }
}
