# GymLog

A gym workout log built with Java and Spring Boot. Users record exercises, sets, reps
and weight for each day, and view their progress over time as a table and a line chart.

**Live demo:** https://gymlog-48en.onrender.com

> The app runs on free hosting and goes to sleep when idle. The first page load can take
> up to a minute. Please wait and refresh if needed.

**Demo accounts**

| Username | Password | Role |
|---|---|---|
| user | user | USER |
| admin | admin | ADMIN |

You can also create your own account on the registration page, or log in with your GitHub account.

## Features

- Log workouts (date, exercise, sets, reps, weight) and edit or delete them
- Manage exercises (add, edit, delete)
- Progress page with a Chart.js line graph of weight over time
- Registration with validation, and login with a username and password or with GitHub
- Workouts and progress are private: each user sees and changes only their own workouts
- Two roles: `USER` manages their own workouts, `ADMIN` can also see and delete everyone's workouts
- Exercises are a shared catalogue that all users can add to; only admins can delete or change them through the REST API, and    only admins can delete them on the website
- Input validation with error messages on the forms
- RESTful JSON endpoints that return only the logged-in user's workouts
- Paged workout list (5 per page)
- Custom error page
- Health check endpoint
- All deletes are POST forms with CSRF protection

## Technologies

- Java 21, Spring Boot 4
- Spring Web MVC and Thymeleaf
- Spring Data JPA and Spring Data REST
- Spring Security with BCrypt password hashing
- Spring Security OAuth2 Client (GitHub login)
- Bean Validation
- MariaDB/MySQL (XAMPP) for local development, PostgreSQL (Neon) online
- Bootstrap, Chart.js
- JUnit 5, MockMvc and Spring Security Test
- Docker, deployed on Render

## Spring Boot features beyond the lectures

- **OAuth2 / social login** with GitHub, with a custom user service that creates a local
  user on the first login
- **Per-user data and ownership checks**: every workout has an owner, and other users'
  workouts are hidden from lists, forms, the progress page and the JSON endpoints
- **Actuator**: health check at `/actuator/health`, also used by Render to monitor the app
- **`@ControllerAdvice`**: central error handling with a custom error page
- **Pagination** with `Pageable` and `PageRequest`
- **Profiles**: separate settings for tests, local development and production
- **Externalized configuration**: database and GitHub credentials come from environment variables
- **Chart.js** integration with Thymeleaf inline JavaScript
- **Docker** multi-stage build and cloud deployment

## Deployment

- **Web app:** Render (free web service), built from the `Dockerfile` in this repository.
  Every push to GitHub triggers a new deployment.
- **Database:** Neon (serverless PostgreSQL).
- The `prod` profile is activated in the Dockerfile. The database connection and the GitHub
  login are configured with the environment variables `DB_URL`, `DB_USER`, `DB_PASSWORD`,
  `GITHUB_CLIENT_ID` and `GITHUB_CLIENT_SECRET` on Render, so no secrets are stored in the
  source code.
- Demo data is created on the first start when the database is empty.

## Running locally

1. Start **MySQL** in XAMPP.
2. Run `GymlogApplication` in VS Code, or run `./mvnw spring-boot:run`.
3. Open http://localhost:8080 (the database `gymlog` is created automatically).

Log in with the demo accounts listed above.

GitHub login needs a GitHub OAuth app with the callback URL
`http://localhost:8080/login/oauth2/code/github`. Set its values as the environment variables
`GITHUB_CLIENT_ID` and `GITHUB_CLIENT_SECRET` before starting the app. Without them the app
runs normally and only the GitHub button does not work.

## Running the tests

With XAMPP MySQL running: `./mvnw test`

They cover the repositories, the REST endpoints, security rules (including admin-only
changes through `/api`), validation, registration, and the rule that users cannot see,
edit or delete each other's workouts.

## REST API

| Address | Description |
|---|---|
| `GET /logs` | Your workouts as JSON (admins get all workouts) |
| `GET /logs/{id}` | One of your workouts as JSON |
| `/api/exercises` | Spring Data REST: exercises (everyone logged in can read, only admins can change) |
| `GET /actuator/health` | Health check (public) |

All addresses except the health check require login. Workouts are not exposed through
Spring Data REST, because that would show every user's data.

## Project structure

```
com.example.gymlog
├── GymlogApplication     start of the app and demo data
├── model/                Exercise, WorkoutLog, AppUser, RegisterForm
├── repository/           database access interfaces
├── controller/           WorkoutController, ExerciseController, RegisterController
├── exception/            ResourceNotFoundException, GlobalExceptionHandler
└── security/             WebSecurityConfig, UserDetailServiceImpl, OAuth2UserServiceImpl
```