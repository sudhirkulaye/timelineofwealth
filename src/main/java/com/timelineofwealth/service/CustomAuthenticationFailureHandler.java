package com.timelineofwealth.service;

import com.timelineofwealth.controllers.PublicViewController;
import com.timelineofwealth.entities.User;
import com.timelineofwealth.repositories.UserRepository;
import com.timelineofwealth.service.AccountService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.authentication.AuthenticationFailureHandler;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Service;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

@Component
@Service
public class CustomAuthenticationFailureHandler implements AuthenticationFailureHandler {

    private static final Logger logger = LoggerFactory.getLogger(PublicViewController.class);

    private final AccountService accountService;

    private static final int MAX_FAILED_ATTEMPTS = 3;
    private Map<String, Integer> failedAttemptsMap = new ConcurrentHashMap<>();

    @Autowired
    private CustomUserDetailsService userDetailsService;

    @Autowired
    public CustomAuthenticationFailureHandler(AccountService accountService) {
        this.accountService = accountService;
    }

    @Autowired
    private UserRepository userRepository;


    @Override
    public void onAuthenticationFailure(HttpServletRequest request, HttpServletResponse response, AuthenticationException exception) throws IOException, ServletException {
        String ipAddress = request.getRemoteAddr();
        String browser = request.getHeader("User-Agent");
        String os = System.getProperty("os.name");
        String username = request.getParameter("username");

        // Increment the failed login attempts for the user
        int failedAttempts = failedAttemptsMap.getOrDefault(username, 0) + 1;

        logger.debug(String.format("In CustomAuthenticationFailureHandler.onAuthenticationFailure: username: " + username + " failuer attempt : " + failedAttempts + ", IP - " + ipAddress + ", browser - " + browser + ", OS - " + os));

        failedAttemptsMap.put(username, failedAttempts);
        if (failedAttempts >= MAX_FAILED_ATTEMPTS) {
            // Lock the user by setting the 'active' attribute to 4 (inactive)
//            User user = (User) userDetailsService.loadUserByUsername(username);
            Optional<User> optionalUser = userRepository.findByEmail(username);
            if (optionalUser.isPresent()) {
                User user = optionalUser.get();
                user.setActive(4);
                userRepository.save(user);
            }
        }
        // Check if the user is inactive
        User user = (User) userDetailsService.loadUserByUsername(username);
        if (user.getActive() == 4) {
            // Redirect the user to the login page with an error message for inactive users
            response.sendRedirect("/userlogin?error=inactive");
            return;
        }
        // Redirect the user to the login page with a general error message for failed login attempts
        response.sendRedirect("/userlogin?error=true");
    }
}

