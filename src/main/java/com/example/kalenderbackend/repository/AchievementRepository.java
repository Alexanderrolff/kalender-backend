package com.example.kalenderbackend.repository;

import com.example.kalenderbackend.entity.Achievement;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface AchievementRepository extends JpaRepository<Achievement, Long> {
    List<Achievement> findByUserId(Long userId);
    List<Achievement> findByUserIdAndUnlocked(Long userId, boolean unlocked);
    Optional<Achievement> findByUserIdAndAchievementType(Long userId, Achievement.AchievementType type);
    boolean existsByUserIdAndAchievementType(Long userId, Achievement.AchievementType type);
}
