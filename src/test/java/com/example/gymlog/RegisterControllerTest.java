package com.example.gymlog;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.springSecurity;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.model;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.redirectedUrl;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.view;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.context.WebApplicationContext;

import com.example.gymlog.model.AppUser;
import com.example.gymlog.repository.UserRepository;

/** Tests the registration page: public access, saving, and the validation rules. */
@SpringBootTest
@ActiveProfiles("test")
@Transactional
public class RegisterControllerTest {

    @Autowired
    private WebApplicationContext context;

    @Autowired
    private UserRepository urepository;

    private MockMvc mockMvc;

    @BeforeEach
    public void setUp() {
        mockMvc = MockMvcBuilders.webAppContextSetup(context).apply(springSecurity()).build();
        urepository.deleteAll();
    }

    @Test
    public void registerPageIsPublic() throws Exception {
        mockMvc.perform(get("/register"))
                .andExpect(status().isOk())
                .andExpect(view().name("register"));
    }

    @Test
    public void validRegistrationCreatesUserWithHashedPassword() throws Exception {
        mockMvc.perform(post("/register").with(csrf())
                        .param("username", "Anna")
                        .param("email", "anna@example.com")
                        .param("password", "secret123"))
                .andExpect(redirectedUrl("/login?registered"));

        AppUser saved = urepository.findByUsername("anna");   // stored in lower case
        assertThat(saved).isNotNull();
        assertThat(saved.getRole()).isEqualTo("USER");
        assertThat(saved.getPasswordHash()).isNotEqualTo("secret123");
    }

    @Test
    public void takenUsernameIsRejected() throws Exception {
        urepository.save(new AppUser("taken", "hash", "t@example.com", "USER"));

        mockMvc.perform(post("/register").with(csrf())
                        .param("username", "Taken")
                        .param("email", "other@example.com")
                        .param("password", "secret123"))
                .andExpect(status().isOk())
                .andExpect(view().name("register"))
                .andExpect(model().attributeHasFieldErrors("registerForm", "username"));

        assertThat(urepository.count()).isEqualTo(1);
    }

    @Test
    public void githubPrefixIsNotAllowed() throws Exception {
        mockMvc.perform(post("/register").with(csrf())
                        .param("username", "github_bob")
                        .param("email", "bob@example.com")
                        .param("password", "secret123"))
                .andExpect(model().attributeHasFieldErrors("registerForm", "username"));
    }

    @Test
    public void shortPasswordIsRejected() throws Exception {
        mockMvc.perform(post("/register").with(csrf())
                        .param("username", "carl")
                        .param("email", "carl@example.com")
                        .param("password", "123"))
                .andExpect(model().attributeHasFieldErrors("registerForm", "password"));
    }
}