package com.example.kalenderbackend.repository;

import com.example.kalenderbackend.entity.Challenge;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface ChallengeRepository extends JpaRepository<Challenge, Long> {
    List<Challenge> findByUserId(Long userId);
    List<Challenge> findByUserIdAndCompleted(Long userId, Boolean completed);
}
