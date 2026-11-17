package com.example.gymlog.repository;
import java.util.List;

import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;

import com.example.gymlog.model.Exercise;

/** Database access for exercises. Spring Data creates the code from the method names. */
public interface ExerciseRepository extends CrudRepository<Exercise, Long> {

    List<Exercise> findByName(@Param("name") String name);

    List<Exercise> findByNameContainingIgnoreCase(@Param("name") String name);
}