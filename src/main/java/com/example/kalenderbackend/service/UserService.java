package com.example.kalenderbackend.service;

import com.example.kalenderbackend.entity.User;
import com.example.kalenderbackend.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
public class UserService {

    @Autowired
    private UserRepository userRepository;

    // XP required for each level (exponential growth)
    private static final int BASE_XP = 100;
    private static final double XP_MULTIPLIER = 1.5;

    public User getUserById(Long id) {
        return userRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("User not found with id: " + id));
    }

    public User getUserByUsername(String username) {
        return userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("User not found: " + username));
    }

    @Transactional
    public User addXP(Long userId, int xpToAdd) {
        User user = getUserById(userId);
        int currentXP = user.getExperiencePoints();
        int newTotalXP = currentXP + xpToAdd;

        user.setExperiencePoints(newTotalXP);

        // Check for level up
        int newLevel = calculateLevel(newTotalXP);
        if (newLevel > user.getLevel()) {
            user.setLevel(newLevel);
            // Could trigger level-up event here
        }

        return userRepository.save(user);
    }

    public int calculateLevel(int totalXP) {
        int level = 1;
        int xpForNextLevel = BASE_XP;
        int accumulatedXP = 0;

        while (accumulatedXP + xpForNextLevel <= totalXP) {
            accumulatedXP += xpForNextLevel;
            level++;
            xpForNextLevel = (int)(xpForNextLevel * XP_MULTIPLIER);
        }

        return level;
    }

    public int getXPForLevel(int level) {
        if (level <= 1) return 0;

        int totalXP = 0;
        int xpForLevel = BASE_XP;

        for (int i = 1; i < level; i++) {
            totalXP += xpForLevel;
            xpForLevel = (int)(xpForLevel * XP_MULTIPLIER);
        }

        return totalXP;
    }

    public int getXPForNextLevel(Long userId) {
        User user = getUserById(userId);
        int nextLevel = user.getLevel() + 1;
        int xpNeededForNext = getXPForLevel(nextLevel);
        return xpNeededForNext - user.getExperiencePoints();
    }

    public double getLevelProgress(Long userId) {
        User user = getUserById(userId);
        int currentLevelXP = getXPForLevel(user.getLevel());
        int nextLevelXP = getXPForLevel(user.getLevel() + 1);
        int xpInCurrentLevel = user.getExperiencePoints() - currentLevelXP;
        int xpNeededForLevel = nextLevelXP - currentLevelXP;

        return (double) xpInCurrentLevel / xpNeededForLevel * 100;
    }

    @Transactional
    public User updateStreak(Long userId, boolean completedToday) {
        User user = getUserById(userId);

        if (completedToday) {
            user.setStreak(user.getStreak() + 1);

            // Update longest streak if needed
            if (user.getStreak() > user.getLongestStreak()) {
                user.setLongestStreak(user.getStreak());
            }
        } else {
            user.setStreak(0); // Reset streak if day was missed
        }

        return userRepository.save(user);
    }

    @Transactional
    public User updateUserProfile(Long userId, String firstName, String lastName, String email) {
        User user = getUserById(userId);

        if (firstName != null) user.setFirstName(firstName);
        if (lastName != null) user.setLastName(lastName);
        if (email != null && !email.equals(user.getEmail())) {
            if (userRepository.existsByEmail(email)) {
                throw new RuntimeException("Email already exists");
            }
            user.setEmail(email);
        }

        return userRepository.save(user);
    }

    public UserStats getUserStats(Long userId) {
        User user = getUserById(userId);

        return UserStats.builder()
                .userId(user.getId())
                .username(user.getUsername())
                .level(user.getLevel())
                .experiencePoints(user.getExperiencePoints())
                .xpForNextLevel(getXPForNextLevel(userId))
                .levelProgress(getLevelProgress(userId))
                .totalEventsCompleted(user.getTotalEventsCompleted())
                .currentStreak(user.getStreak())
                .longestStreak(user.getLongestStreak())
                .challengesCompleted(user.getChallengesCompleted())
                .build();
    }

    @lombok.Builder
    @lombok.Data
    public static class UserStats {
        private Long userId;
        private String username;
        private int level;
        private int experiencePoints;
        private int xpForNextLevel;
        private double levelProgress;
        private int totalEventsCompleted;
        private int currentStreak;
        private int longestStreak;
        private int challengesCompleted;
    }
}
