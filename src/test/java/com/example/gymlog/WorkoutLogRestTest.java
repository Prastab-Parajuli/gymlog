package com.example.gymlog;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.springSecurity;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.model;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.view;

import java.time.LocalDate;

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
import com.example.gymlog.model.WorkoutLog;
import com.example.gymlog.repository.ExerciseRepository;
import com.example.gymlog.repository.WorkoutLogRepository;

@SpringBootTest
@ActiveProfiles("test")
@Transactional
public class WorkoutLogRestTest {

    @Autowired
    private WebApplicationContext context;

    @Autowired
    private WorkoutLogRepository repository;

    @Autowired
    private ExerciseRepository erepository;

    private MockMvc mockMvc;
    private Exercise bench;
    private WorkoutLog log1;

    @BeforeEach
    public void setUp() {
        mockMvc = MockMvcBuilders.webAppContextSetup(context)
                .apply(springSecurity())
                .build();

        repository.deleteAll();
        erepository.deleteAll();

        bench = erepository.save(new Exercise("Bench Press", "Chest"));
        log1 = repository.save(new WorkoutLog(LocalDate.of(2026, 10, 1), 3, 10, 50.0, bench));
        repository.save(new WorkoutLog(LocalDate.of(2026, 10, 8), 3, 10, 55.0, bench));
    }

    // Security

    @Test
    public void notLoggedInUserIsRedirectedToLogin() throws Exception {
        mockMvc.perform(get("/logs")).andExpect(status().is3xxRedirection());
    }

    @Test
    @WithMockUser(roles = "USER")
    public void normalUserCannotDelete() throws Exception {
        mockMvc.perform(get("/delete/" + log1.getId()))
                .andExpect(status().isForbidden());

        assertThat(repository.existsById(log1.getId())).isTrue();
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    public void adminCanDelete() throws Exception {
        mockMvc.perform(get("/delete/" + log1.getId()))
                .andExpect(status().is3xxRedirection());

        assertThat(repository.existsById(log1.getId())).isFalse();
    }

    // Your own REST endpoints

    @Test
    @WithMockUser(roles = "USER")
    public void getLogsShouldReturnJson() throws Exception {
        mockMvc.perform(get("/logs"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(2)));
    }

    @Test
    @WithMockUser(roles = "USER")
    public void getLogByIdShouldReturnLog() throws Exception {
        mockMvc.perform(get("/logs/" + log1.getId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.weight", is(50.0)))
                .andExpect(jsonPath("$.exercise.name", is("Bench Press")));
    }

    // Spring Data REST (/api)

    @Test
    @WithMockUser(roles = "USER")
    public void apiGetLogsShouldReturnAll() throws Exception {
        mockMvc.perform(get("/api/workoutLogs"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$._embedded.workoutLogs", hasSize(2)));
    }

    @Test
    @WithMockUser(roles = "USER")
    public void apiGetUnknownLogShouldReturnNotFound() throws Exception {
        mockMvc.perform(get("/api/workoutLogs/999999"))
                .andExpect(status().isNotFound());
    }
        // Progress page

    @Test
    @WithMockUser(roles = "USER")
    public void progressPageShouldContainChartData() throws Exception {
        mockMvc.perform(get("/progress/" + bench.getId()))
                .andExpect(status().isOk())
                .andExpect(view().name("progress"))
                .andExpect(model().attribute("labels", hasSize(2)))
                .andExpect(model().attribute("weights", hasSize(2)));
    }

        // Error page

    @Test
    @WithMockUser(roles = "USER")
    public void unknownExerciseProgressShouldShowNotFoundPage() throws Exception {
        mockMvc.perform(get("/progress/999999"))
                .andExpect(status().isNotFound())
                .andExpect(view().name("error"));
    }

    // Validation

    @Test
    @WithMockUser(roles = "USER")
    public void invalidFormShouldShowErrorsAndNotSave() throws Exception {
        mockMvc.perform(post("/save").with(csrf())
                        .param("date", "2026-10-28")
                        .param("sets", "0")          // invalid: minimum is 1
                        .param("reps", "10")
                        .param("weight", "60")
                        .param("exerciseId", bench.getId().toString()))
                .andExpect(status().isOk())
                .andExpect(view().name("logform"))
                .andExpect(model().attributeHasFieldErrors("workoutLog", "sets"));

        assertThat(repository.count()).isEqualTo(2);
    }

    @Test
    @WithMockUser(roles = "USER")
    public void validFormShouldSaveAndRedirect() throws Exception {
        mockMvc.perform(post("/save").with(csrf())
                        .param("date", "2026-10-28")
                        .param("sets", "3")
                        .param("reps", "8")
                        .param("weight", "60")
                        .param("exerciseId", bench.getId().toString()))
                .andExpect(status().is3xxRedirection());

        assertThat(repository.count()).isEqualTo(3);
    }
}