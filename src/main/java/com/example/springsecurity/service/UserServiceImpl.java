package com.example.springsecurity.service;

import com.example.springsecurity.dto.UserRegistrationDto;
import com.example.springsecurity.model.Role;
import com.example.springsecurity.model.User;
import com.example.springsecurity.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationListener;
import org.springframework.security.authentication.event.AuthenticationSuccessEvent;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.session.SessionRegistry;
import org.springframework.security.core.session.SessionRegistryImpl;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class UserServiceImpl implements UserService, ApplicationListener<AuthenticationSuccessEvent> {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private BCryptPasswordEncoder passwordEncoder;

    @Autowired
    private SessionService sessionService;

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
    public void block(List<String> users) {
        SessionRegistry sessionRegistry = new SessionRegistryImpl();
        List<Object> objs = sessionRegistry.getAllPrincipals();
        users.stream()
                .map(userEmail -> userRepository.findByEmail(userEmail))
                .peek(user -> user.setActive(false))
                .peek(user -> sessionService.expireUserSessions(user.getEmail()))
                .forEach(user -> userRepository.save(user));
    }

    @Override
    public void unblock(List<String> users) {
        users.stream()
                .map(userEmail -> userRepository.findByEmail(userEmail))
                .peek(user -> user.setActive(true))
                .forEach(user -> userRepository.save(user));
    }

    @Override
    public void delete(List<String> users) {
        users.stream()
                .map(userEmail -> userRepository.findByEmail(userEmail))
                .peek(user -> user.setActive(true))
                .peek(user -> sessionService.expireUserSessions(user.getEmail()))
                .forEach(user -> userRepository.delete(user));
    }
}
