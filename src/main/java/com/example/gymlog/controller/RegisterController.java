package com.example.gymlog.controller;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;

import com.example.gymlog.model.AppUser;
import com.example.gymlog.model.RegisterForm;
import com.example.gymlog.repository.UserRepository;

import jakarta.validation.Valid;

/** Lets a new user create an account. New accounts always get the USER role. */
@Controller
public class RegisterController {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public RegisterController(UserRepository userRepository, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @GetMapping("/register")
    public String showForm(Model model) {
        model.addAttribute("registerForm", new RegisterForm());
        return "register";
    }

    @PostMapping("/register")
    public String register(@Valid @ModelAttribute("registerForm") RegisterForm form,
                           BindingResult bindingResult) {

        // Usernames are stored in lower case so "Anna" and "anna" are the same account
        String username = form.getUsername() == null ? "" : form.getUsername().toLowerCase();

        if (!bindingResult.hasFieldErrors("username") && userRepository.findByUsername(username) != null) {
            bindingResult.rejectValue("username", "taken", "That username is already taken");
        }
        if (bindingResult.hasErrors()) {
            return "register";
        }

        userRepository.save(new AppUser(username, passwordEncoder.encode(form.getPassword()),
                form.getEmail(), "USER"));
        return "redirect:/login?registered";
    }
}