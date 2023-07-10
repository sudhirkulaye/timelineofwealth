package com.timelineofwealth.service;

import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;

@Service
public class AccountService {
    private static final int MAX_FAILED_LOGIN_ATTEMPTS = 3;
    private Map<String, Integer> failedLoginAttempts = new HashMap<>();

    public void incrementFailedLoginAttempts(String username) {
        int attempts = failedLoginAttempts.getOrDefault(username, 0) + 1;
        failedLoginAttempts.put(username, attempts);
        if (attempts >= MAX_FAILED_LOGIN_ATTEMPTS) {
            lockAccount(username);
        }
    }

    private void lockAccount(String username) {
        // Implement account lockout logic here
        // You can update a flag in the user account or set an expiry timestamp, etc.
        // In this example, we are using a simple print statement.
        System.out.println("Account locked: " + username);
    }
}


