package com.example.gymlog.repository;

import java.time.LocalDate;
import java.util.List;

import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import com.example.gymlog.model.WorkoutLog;

/** Database access for workouts, including searches by exercise and date, and paging. */

public interface WorkoutLogRepository extends CrudRepository<WorkoutLog, Long> {

    List<WorkoutLog> findAllByOrderByDateDesc();

    List<WorkoutLog> findByExerciseIdOrderByDateAsc(@Param("exerciseId") Long exerciseId);

    List<WorkoutLog> findByExerciseName(@Param("name") String name);

    List<WorkoutLog> findByDate(@Param("date") LocalDate date);

        /** One page of workouts, newest first (id breaks ties so pages stay stable). */
    Page<WorkoutLog> findAllByOrderByDateDescIdDesc(Pageable pageable);
}