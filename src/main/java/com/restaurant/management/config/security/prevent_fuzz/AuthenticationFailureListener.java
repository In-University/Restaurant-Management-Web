package com.restaurant.management.config.security.prevent_fuzz;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.event.EventListener;
import org.springframework.security.authentication.event.AbstractAuthenticationFailureEvent;
import org.springframework.security.authentication.event.AuthenticationFailureBadCredentialsEvent;
import org.springframework.security.authentication.event.AuthenticationFailureLockedEvent;
import org.springframework.stereotype.Component;

@Component
public class AuthenticationFailureListener {

    @Autowired
    private LoginAttemptService loginAttemptService;

    // Lắng nghe các sự kiện đăng nhập thất bại phổ biến
    // AuthenticationFailureLockedEvent xảy ra khi CustomUserDetailsService ném LockedException
    @EventListener({AuthenticationFailureBadCredentialsEvent.class, AuthenticationFailureLockedEvent.class})
    public void onAuthenticationFailure(AbstractAuthenticationFailureEvent event) {
        String usernameInfo = "N/A";
        if (event.getAuthentication() != null && event.getAuthentication().getPrincipal() != null) {
            usernameInfo = event.getAuthentication().getPrincipal().toString();
        }
        System.out.println("Authentication failure detected. User attempted (if available): " + usernameInfo +
                ", Failure type: " + event.getClass().getSimpleName() +
                ". Source IP will be processed by LoginAttemptService.");

        // LoginAttemptService sẽ tự lấy IP từ request context
        loginAttemptService.loginFailed();
    }
}