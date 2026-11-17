# GymLog

A gym workout log built with Java and Spring Boot. Users record exercises, sets, reps
and weight for each day, and view their progress over time as a table and a line chart.

**Live demo:** _link added after deployment_

## Features

- Log workouts (date, exercise, sets, reps, weight) and edit or delete them
- Manage exercises (add, edit, delete)
- Progress page with a Chart.js line graph of weight over time
- Login with two roles: `USER` can add and edit, `ADMIN` can also delete
- Input validation with error messages on the forms
- RESTful JSON endpoints
- Paged workout list (5 per page)
- Custom error page
- Health check endpoint

## Technologies

- Java 21, Spring Boot 4
- Spring Web MVC and Thymeleaf
- Spring Data JPA and Spring Data REST
- Spring Security with BCrypt password hashing
- Bean Validation
- MariaDB/MySQL (XAMPP) for local development, PostgreSQL online
- Bootstrap, Chart.js
- JUnit 5, MockMvc and Spring Security Test

## Spring Boot features beyond the lectures

- **Actuator**: health check at `/actuator/health`
- **`@ControllerAdvice`**: central error handling with a custom error page
- **Pagination** with `Pageable` and `PageRequest`
- **Profiles**: separate settings for tests and for production
- **Chart.js** integration with Thymeleaf inline JavaScript
- **Docker** deployment to Render

## Running locally

1. Start **MySQL** in XAMPP.
2. Run `GymlogApplication` in VS Code, or run `./mvnw spring-boot:run`.
3. Open http://localhost:8080 (the database `gymlog` is created automatically).

Demo accounts:

| Username | Password | Role |
|---|---|---|
| user | user | USER |
| admin | admin | ADMIN |

## Running the tests

With XAMPP MySQL running: `./mvnw test`

The tests use a separate database (`gymlog_test`), so the demo data is not touched.
They cover the repositories, the REST endpoints, security rules and validation.

## REST API

| Address | Description |
|---|---|
| `GET /logs` | All workouts as JSON |
| `GET /logs/{id}` | One workout as JSON |
| `/api/workoutLogs` | Spring Data REST: list, create, update, delete workouts |
| `/api/exercises` | Spring Data REST: exercises |
| `GET /actuator/health` | Health check (public) |

All addresses except the health check require login.

## Project structure

```
com.example.gymlog
├── GymlogApplication     start of the app and demo data
├── model/                Exercise, WorkoutLog, AppUser
├── repository/           database access interfaces
├── controller/           WorkoutController, ExerciseController
├── exception/            ResourceNotFoundException, GlobalExceptionHandler
└── security/             WebSecurityConfig, UserDetailServiceImpl
```