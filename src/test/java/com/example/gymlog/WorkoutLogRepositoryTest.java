package com.example.gymlog;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.LocalDate;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import com.example.gymlog.model.Exercise;
import com.example.gymlog.model.WorkoutLog;
import com.example.gymlog.repository.ExerciseRepository;
import com.example.gymlog.repository.WorkoutLogRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;

@SpringBootTest
@ActiveProfiles("test")
@Transactional
public class WorkoutLogRepositoryTest {

    @Autowired
    private WorkoutLogRepository repository;

    @Autowired
    private ExerciseRepository erepository;

    private Exercise bench;
    private Exercise squat;

    @BeforeEach
    public void setUp() {
        repository.deleteAll();
        erepository.deleteAll();

        bench = erepository.save(new Exercise("Bench Press", "Chest"));
        squat = erepository.save(new Exercise("Squat", "Legs"));

        repository.save(new WorkoutLog(LocalDate.of(2026, 10, 1), 3, 10, 50.0, bench));
        repository.save(new WorkoutLog(LocalDate.of(2026, 10, 8), 3, 10, 55.0, bench));
        repository.save(new WorkoutLog(LocalDate.of(2026, 10, 8), 4, 8, 80.0, squat));
    }

    @Test
    public void findByExerciseNameShouldReturnLogs() {
        List<WorkoutLog> logs = repository.findByExerciseName("Bench Press");
        assertThat(logs).hasSize(2);
    }

    @Test
    public void progressShouldBeOrderedByDate() {
        List<WorkoutLog> logs = repository.findByExerciseIdOrderByDateAsc(bench.getId());

        assertThat(logs).hasSize(2);
        assertThat(logs.get(0).getWeight()).isEqualTo(50.0);
        assertThat(logs.get(1).getWeight()).isEqualTo(55.0);
    }

    @Test
    public void findByDateShouldReturnLogs() {
        assertThat(repository.findByDate(LocalDate.of(2026, 10, 8))).hasSize(2);
    }

    @Test
    public void createNewLog() {
        WorkoutLog log = repository.save(new WorkoutLog(LocalDate.now(), 5, 5, 100.0, squat));

        assertThat(log.getId()).isNotNull();
        assertThat(repository.count()).isEqualTo(4);
    }

    @Test
    public void deleteLog() {
        WorkoutLog log = repository.findByExerciseName("Squat").get(0);

        repository.deleteById(log.getId());

        assertThat(repository.findByExerciseName("Squat")).isEmpty();
        assertThat(repository.count()).isEqualTo(2);
    }

    @Test
    public void paginationShouldReturnOnePageAtATime() {
        Page<WorkoutLog> page = repository.findAllByOrderByDateDescIdDesc(PageRequest.of(0, 2));

        assertThat(page.getContent()).hasSize(2);
        assertThat(page.getTotalElements()).isEqualTo(3);
        assertThat(page.getTotalPages()).isEqualTo(2);
    }
}