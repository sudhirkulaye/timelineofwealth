package com.timelineofwealth.service;

import com.timelineofwealth.entities.CustomUserDetails;
import com.timelineofwealth.entities.User;
import com.timelineofwealth.repositories.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
@EnableCaching
public class CustomUserDetailsService implements UserDetailsService {
    @Autowired
    private UserRepository userRepository;

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        Optional<User> optionalUser = userRepository.findByEmail(username);

        optionalUser
                .orElseThrow(() -> new UsernameNotFoundException("User Login Id Not Found"));

        return optionalUser.map(CustomUserDetails::new).get();
    }
}
