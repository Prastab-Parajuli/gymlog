package com.example.gymlog;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.springSecurity;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.context.WebApplicationContext;

import com.example.gymlog.model.Exercise;
import com.example.gymlog.repository.ExerciseRepository;

/** Checks who may use the Spring Data REST address /api/exercises. */
@SpringBootTest
@ActiveProfiles("test")
@Transactional
public class ApiSecurityTest {

    @Autowired
    private WebApplicationContext context;

    @Autowired
    private ExerciseRepository erepository;

    private MockMvc mockMvc;
    private Exercise squat;

    @BeforeEach
    public void setUp() {
        mockMvc = MockMvcBuilders.webAppContextSetup(context).apply(springSecurity()).build();
        erepository.deleteAll();
        squat = erepository.save(new Exercise("Squat", "Legs"));
    }

    @Test
    @WithMockUser(username = "user", roles = "USER")
    public void userCanReadExercisesThroughApi() throws Exception {
        mockMvc.perform(get("/api/exercises")).andExpect(status().isOk());
    }

    @Test
    @WithMockUser(username = "user", roles = "USER")
    public void userCannotDeleteExerciseThroughApi() throws Exception {
        mockMvc.perform(delete("/api/exercises/" + squat.getId()).with(csrf()))
                .andExpect(status().isForbidden());

        assertThat(erepository.existsById(squat.getId())).isTrue();
    }

    @Test
    @WithMockUser(username = "admin", roles = "ADMIN")
    public void adminCanDeleteExerciseThroughApi() throws Exception {
        mockMvc.perform(delete("/api/exercises/" + squat.getId()).with(csrf()))
                .andExpect(status().isNoContent());

        assertThat(erepository.existsById(squat.getId())).isFalse();
    }
}