package com.restaurant.management.config.security.prevent_fuzz;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationListener;
import org.springframework.security.authentication.event.AuthenticationSuccessEvent;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;

@Component
public class AuthenticationSuccessListener implements ApplicationListener<AuthenticationSuccessEvent> {

    @Autowired
    private LoginAttemptService loginAttemptService;

    @Override
    public void onApplicationEvent(AuthenticationSuccessEvent event) {
        String username = "N/A";
        if (event.getAuthentication() != null && event.getAuthentication().getPrincipal() instanceof UserDetails) {
            username = ((UserDetails) event.getAuthentication().getPrincipal()).getUsername();
        } else if (event.getAuthentication() != null && event.getAuthentication().getPrincipal() instanceof String) {
            username = (String) event.getAuthentication().getPrincipal();
        }
        System.out.println("Authentication success for user: " + username + ". Source IP will be processed by LoginAttemptService.");

        // LoginAttemptService sẽ tự lấy IP từ request context
        loginAttemptService.loginSucceeded();
    }
}