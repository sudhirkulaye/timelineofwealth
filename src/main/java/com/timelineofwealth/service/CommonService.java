package com.timelineofwealth.service;

import com.timelineofwealth.controllers.UserViewController;
import com.timelineofwealth.entities.User;
import com.timelineofwealth.repositories.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class CommonService {

    public static final String ADMIN_ROLE = "ROLE_ADMIN";
    public static final String ADVISER_ROLE = "ROLE_ADVISER";
    private static final Logger logger = LoggerFactory.getLogger(CommonService.class);

    @Autowired
    private static UserRepository userRepository;

    @Autowired
    public  void setUserRepository(UserRepository userRepository){
        CommonService.userRepository = userRepository;
    }

    /**
     * Returns true if SignIn User is Admin
     * @param userDetails
     * @return
     */
    public static boolean isAdmin(@AuthenticationPrincipal UserDetails userDetails){
        boolean isAdmin = false;
        String roleName = getLoggedInUser(userDetails).getRoleName();
        if (roleName.equals(ADMIN_ROLE)) {
            isAdmin = true;
        }
        return isAdmin;
    }

    /**
     * Returns true if SignIn User is Adviser
     * @param userDetails
     * @return
     */
    public static boolean isAdviser(@AuthenticationPrincipal UserDetails userDetails){
        boolean isAdviser = false;
        String roleName = getLoggedInUser(userDetails).getRoleName();
        if (roleName.equals(ADVISER_ROLE)) {
            isAdviser = true;
        }
        return isAdviser;
    }

    public static void updateLastLoginStatus(@AuthenticationPrincipal UserDetails userDetails){
        User loggedInUser = getLoggedInUser(userDetails);
        loggedInUser.setLastLoginTime(new java.sql.Timestamp(new java.util.Date().getTime()));
        userRepository.save(loggedInUser);
    }

    /**
     * Returns User Object for logged-in user
     * @param userDetails
     * @return
     * @throws UsernameNotFoundException
     */
    public static User getLoggedInUser(UserDetails userDetails) throws  UsernameNotFoundException {
        Optional<User> optionalUser = userRepository.findByEmail(userDetails.getUsername());

        optionalUser
                .orElseThrow(() -> new UsernameNotFoundException("User Login Id Not Found"));

        return optionalUser.map(User::new).get();

    }

    /**
     * Returns Welcome message to be displayed after signIn
     * @param signInUser
     * @return
     */
    public static String getWelcomeMessage(User signInUser){
        String welcomeMessage = "";
        if (signInUser != null) {
            welcomeMessage = signInUser.getPrefix()+ " "+ signInUser.getLastName();
        }
        return welcomeMessage;
    }

}
