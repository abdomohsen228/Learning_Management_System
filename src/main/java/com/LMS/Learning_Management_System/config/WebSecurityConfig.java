package com.LMS.Learning_Management_System.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
@EnableWebSecurity
public class WebSecurityConfig {

    @Bean

    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers("/api/users/**", "/api/student/**" , "/api/instructor/**").permitAll()
                        .requestMatchers("/api/auth/**").permitAll()
                        .requestMatchers("/api/enrollment/**").permitAll()
                        .requestMatchers("/api/course/**").permitAll()
                        .requestMatchers("/api/lesson/**").permitAll()
                        .requestMatchers("/api/quiz/**").permitAll()
                        .requestMatchers("/api/assignment/**").permitAll()
                        .anyRequest().authenticated()
                )
                .cors(cors -> cors.disable())
                .csrf().disable()
                .logout(logout -> logout
                        .logoutUrl("/api/auth/logout")
                        .clearAuthentication(true)
                        .invalidateHttpSession(true)
                );

        return http.build();
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
}
