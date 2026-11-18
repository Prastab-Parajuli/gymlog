package com.example.gymlog;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.springSecurity;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.model;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.view;

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
import com.example.gymlog.repository.WorkoutLogRepository;

@SpringBootTest
@ActiveProfiles("test")
@Transactional
public class ExerciseControllerTest {

    @Autowired
    private WebApplicationContext context;

    @Autowired
    private ExerciseRepository erepository;

    @Autowired
    private WorkoutLogRepository repository;

    private MockMvc mockMvc;
    private Exercise bench;

    @BeforeEach
    public void setUp() {
        mockMvc = MockMvcBuilders.webAppContextSetup(context)
                .apply(springSecurity())
                .build();

        repository.deleteAll();
        erepository.deleteAll();

        bench = erepository.save(new Exercise("Bench Press", "Chest"));
    }

    @Test
    public void notLoggedInUserIsRedirectedToLogin() throws Exception {
        mockMvc.perform(get("/exercises")).andExpect(status().is3xxRedirection());
    }

    @Test
    @WithMockUser(roles = "USER")
    public void exerciseListShouldShowPage() throws Exception {
        mockMvc.perform(get("/exercises"))
                .andExpect(status().isOk())
                .andExpect(view().name("exerciselist"))
                .andExpect(model().attributeExists("exercises"));
    }

    @Test
    @WithMockUser(roles = "USER")
    public void blankNameShouldShowErrorsAndNotSave() throws Exception {
        mockMvc.perform(post("/saveexercise").with(csrf())
                        .param("name", "")
                        .param("muscleGroup", "Back"))
                .andExpect(status().isOk())
                .andExpect(view().name("exerciseform"))
                .andExpect(model().attributeHasFieldErrors("exercise", "name"));

        assertThat(erepository.count()).isEqualTo(1);
    }

    @Test
    @WithMockUser(roles = "USER")
    public void validExerciseShouldBeSaved() throws Exception {
        mockMvc.perform(post("/saveexercise").with(csrf())
                        .param("name", "Pull Up")
                        .param("muscleGroup", "Back"))
                .andExpect(status().is3xxRedirection());

        assertThat(erepository.findByName("Pull Up")).hasSize(1);
    }

    @Test
    @WithMockUser(roles = "USER")
    public void editShouldChangeName() throws Exception {
        mockMvc.perform(post("/saveexercise").with(csrf())
                        .param("id", bench.getId().toString())
                        .param("name", "Incline Bench")
                        .param("muscleGroup", "Chest"))
                .andExpect(status().is3xxRedirection());

        assertThat(erepository.findById(bench.getId()).get().getName()).isEqualTo("Incline Bench");
    }

    @Test
    @WithMockUser(roles = "USER")
    public void normalUserCannotDeleteExercise() throws Exception {
        mockMvc.perform(post("/deleteexercise/" + bench.getId()).with(csrf()))
                .andExpect(status().isForbidden());

        assertThat(erepository.existsById(bench.getId())).isTrue();
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    public void adminCanDeleteExercise() throws Exception {
        mockMvc.perform(post("/deleteexercise/" + bench.getId()).with(csrf()))
                .andExpect(status().is3xxRedirection());

        assertThat(erepository.existsById(bench.getId())).isFalse();
    }
}