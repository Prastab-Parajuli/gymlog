package com.example.gymlog;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import com.example.gymlog.model.AppUser;
import com.example.gymlog.repository.UserRepository;

@SpringBootTest
@ActiveProfiles("test")
@Transactional
public class UserRepositoryTest {

    @Autowired
    private UserRepository urepository;

    @BeforeEach
    public void setUp() {
        urepository.deleteAll();

        urepository.save(new AppUser("user", "hash1", "user@example.com", "USER"));
        urepository.save(new AppUser("admin", "hash2", "admin@example.com", "ADMIN"));
    }

    @Test
    public void findByUsernameShouldReturnUser() {
        AppUser user = urepository.findByUsername("admin");

        assertThat(user).isNotNull();
        assertThat(user.getEmail()).isEqualTo("admin@example.com");
        assertThat(user.getRole()).isEqualTo("ADMIN");
    }

    @Test
    public void findByUsernameShouldReturnNullWhenNotFound() {
        assertThat(urepository.findByUsername("ghost")).isNull();
    }

    @Test
    public void createNewUser() {
        AppUser user = urepository.save(new AppUser("alice", "hash3", "alice@example.com", "USER"));

        assertThat(user.getId()).isNotNull();
        assertThat(urepository.count()).isEqualTo(3);
    }
}