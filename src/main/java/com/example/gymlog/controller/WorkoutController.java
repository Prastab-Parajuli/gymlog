package com.example.gymlog.controller;

import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.security.core.Authentication;
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
 * Every user only sees their own workouts. Admins can see and delete everyone's.
 */
@Controller
public class WorkoutController {

    /** Number of workouts shown on one page of the list. */
    private static final int PAGE_SIZE = 5;

    private final WorkoutLogRepository logRepository;
    private final ExerciseRepository exerciseRepository;

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

    // ---------- Ownership helpers ----------

    private boolean isAdmin(Authentication authentication) {
        return authentication.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN"));
    }

    /**
     * Finds a workout the current user may use: their own, or any if they are an admin.
     * Someone else's workout gives "not found", so its existence is not revealed.
     */
    private WorkoutLog findAllowedLog(Long id, Authentication authentication) {
        WorkoutLog log = logRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Workout " + id + " was not found"));
        if (!isAdmin(authentication) && !authentication.getName().equals(log.getOwner())) {
            throw new ResourceNotFoundException("Workout " + id + " was not found");
        }
        return log;
    }

    // ---------- Thymeleaf pages ----------

    /** Shows one page of workouts (5 per page). */
    @GetMapping("/loglist")
    public String logList(@RequestParam(name = "page", defaultValue = "0") int page,
                          Authentication authentication, Model model) {
        int safePage = Math.max(page, 0);
        PageRequest pageRequest = PageRequest.of(safePage, PAGE_SIZE);

        Page<WorkoutLog> logPage = isAdmin(authentication)
                ? logRepository.findAllByOrderByDateDescIdDesc(pageRequest)
                : logRepository.findByOwnerOrderByDateDescIdDesc(authentication.getName(), pageRequest);

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
    public String editLog(@PathVariable("id") Long id, Authentication authentication, Model model) {
        model.addAttribute("workoutLog", findAllowedLog(id, authentication));
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
                       Authentication authentication,
                       Model model) {

        Exercise exercise = exerciseRepository.findById(exerciseId)
                .orElseThrow(() -> new ResourceNotFoundException("Exercise " + exerciseId + " was not found"));

        if (bindingResult.hasErrors()) {
            log.setExercise(exercise);   // keeps the chosen exercise selected when the form is shown again
            model.addAttribute("exercises", exerciseRepository.findAll());
            return "logform";
        }

        if (log.getId() == null) {
            log.setOwner(authentication.getName());            // new workout belongs to the current user
        } else {
            WorkoutLog existing = findAllowedLog(log.getId(), authentication);
            log.setOwner(existing.getOwner());                 // an edit keeps the original owner
        }

        log.setExercise(exercise);
        logRepository.save(log);
        return "redirect:/loglist";
    }

    /** Deletes a workout. Allowed for its owner, and for admins. */
    @PostMapping("/delete/{id}")
    public String deleteLog(@PathVariable("id") Long id, Authentication authentication) {
        WorkoutLog log = findAllowedLog(id, authentication);
        logRepository.deleteById(log.getId());
        return "redirect:/loglist";
    }

    // ---------- Progress over time ----------

    /** Shows the progress page: a table and a line chart of the user's weight over time. */
    @GetMapping("/progress/{exerciseId}")
    public String progress(@PathVariable("exerciseId") Long exerciseId,
                           Authentication authentication, Model model) {
        Exercise exercise = exerciseRepository.findById(exerciseId)
                .orElseThrow(() -> new ResourceNotFoundException("Exercise " + exerciseId + " was not found"));
        List<WorkoutLog> logs =
                logRepository.findByExerciseIdAndOwnerOrderByDateAsc(exerciseId, authentication.getName());
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

    /** Returns the user's workouts as JSON (admins get everyone's). */
    @GetMapping("/logs")
    @ResponseBody
    public Iterable<WorkoutLog> getLogsRest(Authentication authentication) {
        return isAdmin(authentication)
                ? logRepository.findAllByOrderByDateDesc()
                : logRepository.findByOwnerOrderByDateDesc(authentication.getName());
    }

    /** Returns one workout as JSON, if it belongs to the user (or the user is an admin). */
    @GetMapping("/logs/{id}")
    @ResponseBody
    public WorkoutLog findLogRest(@PathVariable("id") Long id, Authentication authentication) {
        return findAllowedLog(id, authentication);
    }
}