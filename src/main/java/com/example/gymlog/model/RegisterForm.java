package com.example.gymlog.model;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

/** The data entered on the registration page, with validation rules. */
public class RegisterForm {

    // Letters and digits only. This also means nobody can register a name starting
    // with "github_", which is reserved for GitHub logins.
    @NotBlank(message = "Username is required")
    @Pattern(regexp = "^[A-Za-z0-9]{3,20}$", message = "Username must be 3-20 letters or digits")
    private String username;

    @NotBlank(message = "Email is required")
    @Email(message = "Enter a valid email address")
    private String email;

    @Size(min = 6, max = 50, message = "Password must be 6-50 characters")
    private String password;

    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public String getPassword() { return password; }
    public void setPassword(String password) { this.password = password; }
}