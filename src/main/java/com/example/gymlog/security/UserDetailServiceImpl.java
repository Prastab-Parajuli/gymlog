package com.example.gymlog.security;

import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import com.example.gymlog.model.AppUser;
import com.example.gymlog.repository.UserRepository;
/** Tells Spring Security how to find a user by username in the database. */
@Service
public class UserDetailServiceImpl implements UserDetailsService {

    private final UserRepository repository;

    public UserDetailServiceImpl(UserRepository userRepository) {
        this.repository = userRepository;
    }

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        AppUser curruser = repository.findByUsername(username);

        if (curruser == null) {
            throw new UsernameNotFoundException("User not found: " + username);
        }

        return User.withUsername(username)
                .password(curruser.getPasswordHash())
                .roles(curruser.getRole())
                .build();
    }
}