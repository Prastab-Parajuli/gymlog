package com.example.gymlog;

import java.time.LocalDate;

import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Profile;
import org.springframework.security.crypto.password.PasswordEncoder;

import com.example.gymlog.model.AppUser;
import com.example.gymlog.model.Exercise;
import com.example.gymlog.model.WorkoutLog;
import com.example.gymlog.repository.ExerciseRepository;
import com.example.gymlog.repository.UserRepository;
import com.example.gymlog.repository.WorkoutLogRepository;

@SpringBootApplication
public class GymlogApplication {

    public static void main(String[] args) {
        SpringApplication.run(GymlogApplication.class, args);
    }

    @Bean
    @Profile("!test")
    public CommandLineRunner demo(ExerciseRepository exerciseRepository,
                                  WorkoutLogRepository logRepository,
                                  UserRepository userRepository,
                                  PasswordEncoder passwordEncoder) {
        return args -> {
            // Seed demo data only into an empty database, so restarts keep real data
            if (userRepository.count() > 0) {
                return;
            }

            Exercise bench = exerciseRepository.save(new Exercise("Bench Press", "Chest"));
            Exercise squat = exerciseRepository.save(new Exercise("Squat", "Legs"));
            Exercise deadlift = exerciseRepository.save(new Exercise("Deadlift", "Back"));

            LocalDate today = LocalDate.now();
            logRepository.save(new WorkoutLog(today.minusDays(14), 3, 10, 50.0, bench));
            logRepository.save(new WorkoutLog(today.minusDays(7), 3, 10, 55.0, bench));
            logRepository.save(new WorkoutLog(today, 3, 8, 60.0, bench));
            logRepository.save(new WorkoutLog(today.minusDays(7), 4, 8, 80.0, squat));
            logRepository.save(new WorkoutLog(today, 4, 8, 85.0, squat));
            logRepository.save(new WorkoutLog(today, 3, 5, 100.0, deadlift));

            // Passwords can be set with environment variables on the server
            String userPw = System.getenv().getOrDefault("DEMO_USER_PASSWORD", "user");
            String adminPw = System.getenv().getOrDefault("DEMO_ADMIN_PASSWORD", "admin");

            userRepository.save(new AppUser("user", passwordEncoder.encode(userPw), "user@example.com", "USER"));
            userRepository.save(new AppUser("admin", passwordEncoder.encode(adminPw), "admin@example.com", "ADMIN"));
        };
    }
}