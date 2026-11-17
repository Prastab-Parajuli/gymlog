package com.example.gymlog.controller;

import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;

import com.example.gymlog.exception.ResourceNotFoundException;
import com.example.gymlog.model.Exercise;
import com.example.gymlog.repository.ExerciseRepository;

import jakarta.validation.Valid;

/**
 * Pages for managing exercises (add, edit, delete).
 */
@Controller
public class ExerciseController {

    private final ExerciseRepository exerciseRepository;

    public ExerciseController(ExerciseRepository exerciseRepository) {
        this.exerciseRepository = exerciseRepository;
    }

    /** Shows the list of all exercises. */
    @GetMapping("/exercises")
    public String list(Model model) {
        model.addAttribute("exercises", exerciseRepository.findAll());
        return "exerciselist";
    }

    /** Shows an empty form for a new exercise. */
    @GetMapping("/addexercise")
    public String add(Model model) {
        model.addAttribute("exercise", new Exercise());
        return "exerciseform";
    }

    /** Shows the form filled with an existing exercise. */
        @GetMapping("/editexercise/{id}")
    public String edit(@PathVariable("id") Long id, Model model) {
        Exercise exercise = exerciseRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Exercise " + id + " was not found"));
        model.addAttribute("exercise", exercise);
        return "exerciseform";
    }

    /** Saves a new or edited exercise. If the input is invalid, shows the form again. */
    @PostMapping("/saveexercise")
    public String save(@Valid @ModelAttribute("exercise") Exercise exercise,
                       BindingResult bindingResult) {

        if (bindingResult.hasErrors()) {
            return "exerciseform";
        }

        Exercise toSave = exercise;
        if (exercise.getId() != null) {
            // Editing: update the stored exercise so its workouts stay linked
            Exercise existing = exerciseRepository.findById(exercise.getId()).orElse(null);
            if (existing != null) {
                existing.setName(exercise.getName());
                existing.setMuscleGroup(exercise.getMuscleGroup());
                toSave = existing;
            }
        }
        exerciseRepository.save(toSave);
        return "redirect:/exercises";
    }

    /** Deletes an exercise and all its workouts. Admin only. */
    @GetMapping("/deleteexercise/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public String delete(@PathVariable("id") Long id) {
        exerciseRepository.deleteById(id);
        return "redirect:/exercises";
    }
}