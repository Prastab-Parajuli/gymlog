package com.example.gymlog.model;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonIgnore;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import jakarta.validation.constraints.NotBlank;


/** An exercise (for example Bench Press) that workouts are logged for. */
@Entity
public class Exercise {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank(message = "Exercise name is required")
    private String name;

    private String muscleGroup;

    @JsonIgnore
    @OneToMany(mappedBy = "exercise", cascade = CascadeType.ALL)
    private List<WorkoutLog> logs;

    public Exercise() {
    }

    public Exercise(String name, String muscleGroup) {
        this.name = name;
        this.muscleGroup = muscleGroup;
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getMuscleGroup() { return muscleGroup; }
    public void setMuscleGroup(String muscleGroup) { this.muscleGroup = muscleGroup; }

    public List<WorkoutLog> getLogs() { return logs; }
    public void setLogs(List<WorkoutLog> logs) { this.logs = logs; }
}