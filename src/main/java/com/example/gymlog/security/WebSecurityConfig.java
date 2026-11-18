package com.example.gymlog.security;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;

/** Security settings: login page (form + GitHub), public addresses, logout and password encoding. */
@Configuration
@EnableMethodSecurity(securedEnabled = true)
public class WebSecurityConfig {

    @Bean
    public SecurityFilterChain configure(HttpSecurity http, OAuth2UserServiceImpl oauthService) throws Exception {
        http
            .authorizeHttpRequests(authorize -> authorize
                .requestMatchers("/css/**", "/error", "/actuator/health", "/register").permitAll()
                // Spring Data REST (/api): everyone logged in may read, only admins may change data
                .requestMatchers(HttpMethod.GET, "/api/**").authenticated()
                .requestMatchers("/api/**").hasRole("ADMIN")
                .anyRequest().authenticated()
            )
            .formLogin(formlogin -> formlogin
                .loginPage("/login")
                .defaultSuccessUrl("/loglist", true)
                .permitAll()
            )
            // "Log in with GitHub": after GitHub confirms the user, our own service
            // creates or loads the matching local user
            .oauth2Login(oauth -> oauth
                .loginPage("/login")
                .defaultSuccessUrl("/loglist", true)
                .userInfoEndpoint(info -> info.userService(oauthService))
            )
            .logout(logout -> logout
                .invalidateHttpSession(true)
                .permitAll()
            );
        return http.build();
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
}