package com.example.gymlog.controller;

import java.util.List;
import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import com.example.gymlog.exception.ResourceNotFoundException;
import com.example.gymlog.model.Exercise;
import com.example.gymlog.model.WorkoutLog;
import com.example.gymlog.repository.ExerciseRepository;
import com.example.gymlog.repository.WorkoutLogRepository;

import jakarta.validation.Valid;

/**
 * Handles the workout pages (list, add, edit, delete, progress)
 * and the JSON endpoints for workouts.
 */
@Controller
public class WorkoutController {

    /** Number of workouts shown on one page of the list. */

    private final WorkoutLogRepository logRepository;
    private final ExerciseRepository exerciseRepository;
    private static final int PAGE_SIZE = 5;

    public WorkoutController(WorkoutLogRepository logRepository,
                             ExerciseRepository exerciseRepository) {
        this.logRepository = logRepository;
        this.exerciseRepository = exerciseRepository;
    }

    /** Shows the login page. */
    @GetMapping("/login")
    public String login() {
        return "login";
    }

    /** The start address sends the user to the workout list. */
    @GetMapping("/")
    public String home() {
        return "redirect:/loglist";
    }

    // ---------- Thymeleaf pages ----------

        /** Shows one page of workouts (5 per page). */
    @GetMapping("/loglist")
    public String logList(@RequestParam(name = "page", defaultValue = "0") int page, Model model) {
        int safePage = Math.max(page, 0);
        Page<WorkoutLog> logPage =
                logRepository.findAllByOrderByDateDescIdDesc(PageRequest.of(safePage, PAGE_SIZE));

        model.addAttribute("logs", logPage.getContent());
        model.addAttribute("currentPage", safePage);
        model.addAttribute("totalPages", logPage.getTotalPages());
        model.addAttribute("exercises", exerciseRepository.findAll());
        return "loglist";
    }

    /** Shows an empty form for a new workout. */
    @GetMapping("/addlog")
    public String addLog(Model model) {
        model.addAttribute("workoutLog", new WorkoutLog());
        model.addAttribute("exercises", exerciseRepository.findAll());
        return "logform";
    }

    /** Shows the form filled with an existing workout. */
        @GetMapping("/edit/{id}")
        public String editLog(@PathVariable("id") Long id, Model model) {
            WorkoutLog log = logRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Workout " + id + " was not found"));
            model.addAttribute("workoutLog", log);
            model.addAttribute("exercises", exerciseRepository.findAll());
            return "logform";
    }

    /**
     * Saves a new or edited workout (the id is empty for new workouts).
     * If the input is invalid, the form is shown again with error messages.
     */
    @PostMapping("/save")
    public String save(@Valid @ModelAttribute("workoutLog") WorkoutLog log,
                       BindingResult bindingResult,
                       @RequestParam("exerciseId") Long exerciseId,
                       Model model) {

        if (bindingResult.hasErrors()) {
            model.addAttribute("exercises", exerciseRepository.findAll());
            return "logform";
        }

        log.setExercise(exerciseRepository.findById(exerciseId).orElse(null));
        logRepository.save(log);
        return "redirect:/loglist";
    }

    /** Deletes a workout. Only users with the ADMIN role are allowed. */
    @GetMapping("/delete/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public String deleteLog(@PathVariable("id") Long id) {
        logRepository.deleteById(id);
        return "redirect:/loglist";
    }

    // ---------- Progress over time ----------

    /** Shows the progress page: a table and a line chart of weight over time. */
    @GetMapping("/progress/{exerciseId}")
    public String progress(@PathVariable("exerciseId") Long exerciseId, Model model) {
        Exercise exercise = exerciseRepository.findById(exerciseId)
                .orElseThrow(() -> new ResourceNotFoundException("Exercise " + exerciseId + " was not found"));
        List<WorkoutLog> logs = logRepository.findByExerciseIdOrderByDateAsc(exerciseId);
        double best = logs.stream().mapToDouble(WorkoutLog::getWeight).max().orElse(0);

        // Data for the Chart.js graph: dates on the x-axis, weights on the y-axis
        List<String> labels = logs.stream().map(l -> l.getDate().toString()).toList();
        List<Double> weights = logs.stream().map(WorkoutLog::getWeight).toList();

        model.addAttribute("exercise", exercise);
        model.addAttribute("logs", logs);
        model.addAttribute("bestWeight", best);
        model.addAttribute("labels", labels);
        model.addAttribute("weights", weights);
        return "progress";
    }

    // ---------- REST (JSON) ----------

    /** Returns all workouts as JSON. */
    @GetMapping("/logs")
    @ResponseBody
    public Iterable<WorkoutLog> getLogsRest() {
        return logRepository.findAll();
    }

    /** Returns one workout as JSON. */
    @GetMapping("/logs/{id}")
    @ResponseBody
    public Optional<WorkoutLog> findLogRest(@PathVariable("id") Long id) {
        return logRepository.findById(id);
    }
}