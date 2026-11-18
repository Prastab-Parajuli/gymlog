package com.example.gymlog.model;

import java.time.LocalDate;

import org.springframework.format.annotation.DateTimeFormat;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.ManyToOne;
import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

/** One logged workout: date, sets, reps and weight for one exercise. */
@Entity
public class WorkoutLog {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotNull(message = "Date is required")
    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
    private LocalDate date;

    @Min(value = 1, message = "Sets must be at least 1")
    @Max(value = 50, message = "Sets must be 50 or less")
    private int sets;

    @Min(value = 1, message = "Reps must be at least 1")
    @Max(value = 200, message = "Reps must be 200 or less")
    private int reps;

    @DecimalMin(value = "0.0", message = "Weight cannot be negative")
    @DecimalMax(value = "1000.0", message = "Weight must be 1000 kg or less")
    private double weight;

    @ManyToOne
    private Exercise exercise;

    /** Username of the person who owns this workout. */
    @Column(name = "owner_username")
    private String owner;

    public WorkoutLog() {
    }

    public WorkoutLog(LocalDate date, int sets, int reps, double weight, Exercise exercise) {
        this.date = date;
        this.sets = sets;
        this.reps = reps;
        this.weight = weight;
        this.exercise = exercise;
    }

    public WorkoutLog(LocalDate date, int sets, int reps, double weight, Exercise exercise, String owner) {
        this(date, sets, reps, weight, exercise);
        this.owner = owner;
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public LocalDate getDate() { return date; }
    public void setDate(LocalDate date) { this.date = date; }

    public int getSets() { return sets; }
    public void setSets(int sets) { this.sets = sets; }

    public int getReps() { return reps; }
    public void setReps(int reps) { this.reps = reps; }

    public double getWeight() { return weight; }
    public void setWeight(double weight) { this.weight = weight; }

    public Exercise getExercise() { return exercise; }
    public void setExercise(Exercise exercise) { this.exercise = exercise; }

    public String getOwner() { return owner; }
    public void setOwner(String owner) { this.owner = owner; }
}