package com.restaurant.management.config.security;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.provisioning.InMemoryUserDetailsManager;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
public class SecurityConfig {
    @Autowired
    private CustomUserDetailsService customUserDetailsService;

    private final String[] ADMIN_ENDPOINTS = {
            "/inventories/**",
            "/tables",
            "/employees/**",
            "/customers",
            "/dashboard",
            "/shifts",
            "/suppliers/**",
            "/dishes",
            "/recipes/**",
            "/schedules",
            "/reservations",
    };

    private final String[] PUBLIC_Endpoints = {
            "/login",
            "/request-otp",
            "/register",
            "/verify-otp",
            "/forgot-password",
            "/resources/**", "/css/**", "/js/**",
            "/"
    };


    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
            .authorizeHttpRequests(authorize -> authorize
                    .requestMatchers(PUBLIC_Endpoints).permitAll()
                    .requestMatchers(ADMIN_ENDPOINTS).hasRole("ADMIN")
                    .anyRequest().authenticated()
            )
            .formLogin(form -> form
                    .loginPage("/login")
                    .defaultSuccessUrl("/profile", true)
                    .permitAll()
            )
            .logout(logout -> logout
                .logoutUrl("/logout")
                .logoutSuccessUrl("/login?logout")
                .invalidateHttpSession(true)
                .addLogoutHandler((request, response, authentication) -> {
                    jakarta.servlet.http.Cookie cookie = new jakarta.servlet.http.Cookie("JSESSIONID", "");
                    cookie.setMaxAge(0);
                    cookie.setPath("/");
                    cookie.setHttpOnly(true);
                    response.addCookie(cookie);
                })
            )
            .userDetailsService(customUserDetailsService)
            .sessionManagement(session -> session
                    .maximumSessions(1)
                    .maxSessionsPreventsLogin(false)
            );
            // .headers(headers -> headers
            //         .contentSecurityPolicy(csp -> csp
            //                 .policyDirectives(
            //                         "default-src 'self'; " +
            //                                 "script-src 'self' https://cdn.tailwindcss.com https://unpkg.com https://cdn.jsdelivr.net 'sha256-f5D0+4Q6y/bEGlQNyNwjffhtn5n2eGnMTfszd/Lv5PU='; " +
            //                                 "style-src 'self' https://fonts.googleapis.com https://cdnjs.cloudflare.com ; " +
            //                                 "img-src 'self' data: https://res.cloudinary.com https://images.unsplash.com;; " +
            //                                 "font-src 'self' https://fonts.gstatic.com https://cdnjs.cloudflare.com; " +
            //                                 "connect-src 'self'; " +
            //                                 "frame-ancestors 'none'; " +
            //                                 "form-action 'self'; " +
            //                                 "base-uri 'self'"
            //                 )
            //         )
            // );

        // http.csrf(AbstractHttpConfigurer::disable);
        return http.build();
    }

    @Bean
    public UserDetailsService userDetailsService() {
        return customUserDetailsService;
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

}
