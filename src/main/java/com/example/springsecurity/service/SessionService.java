package com.example.springsecurity.service;

public interface SessionService {
    public void expireUserSessions(String username);
}
