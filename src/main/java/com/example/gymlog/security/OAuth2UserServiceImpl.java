package com.example.gymlog.security;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.client.userinfo.DefaultOAuth2UserService;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.user.DefaultOAuth2User;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Service;

import com.example.gymlog.model.AppUser;
import com.example.gymlog.repository.UserRepository;

/**
 * Handles "Log in with GitHub". The first time someone logs in, a local AppUser
 * is created for them in our own database with the role USER.
 */
@Service
public class OAuth2UserServiceImpl extends DefaultOAuth2UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public OAuth2UserServiceImpl(UserRepository userRepository, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    public OAuth2User loadUser(OAuth2UserRequest request) throws OAuth2AuthenticationException {
        OAuth2User githubUser = super.loadUser(request);
        String login = githubUser.getAttribute("login");

        // The "github_" prefix stops a GitHub user named "admin" from being
        // treated as our local admin account
        String username = "github_" + login;

        if (userRepository.findByUsername(username) == null) {
            userRepository.save(new AppUser(
                    username,
                    passwordEncoder.encode(UUID.randomUUID().toString()), // nobody knows it, so no password login
                    login + "@users.noreply.github.com",
                    "USER"));
        }

        Map<String, Object> attributes = new HashMap<>(githubUser.getAttributes());
        attributes.put("username", username);

        return new DefaultOAuth2User(
                List.of(new SimpleGrantedAuthority("ROLE_USER")),
                attributes,
                "username");
    }
}