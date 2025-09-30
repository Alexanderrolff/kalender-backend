package com.example.kalenderbackend.repository;

import com.example.kalenderbackend.entity.User;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
class UserRepositoryTest {

    @Autowired
    private UserRepository userRepository;

    @Test
    void shouldSaveAndFindUser() {
        // Given
        User user = new User();
        user.setEmail("test@livskraft.se");
        user.setUsername("TestAnvändare");

        // When
        User savedUser = userRepository.save(user);
        User foundUser = userRepository.findById(savedUser.getId()).orElse(null);

        // Then
        assertThat(foundUser).isNotNull();
        assertThat(foundUser.getEmail()).isEqualTo("test@livskraft.se");
        assertThat(foundUser.getUsername()).isEqualTo("TestAnvändare");
        assertThat(foundUser.getLevel()).isEqualTo(1);
        assertThat(foundUser.getXp()).isEqualTo(0);
    }
}
