package com.example.kalenderbackend.controller;

import com.example.kalenderbackend.entity.User;
import com.example.kalenderbackend.repository.UserRepository;
import com.example.kalenderbackend.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/users")
@CrossOrigin(origins = "http://localhost:3000")
public class UserController {

    @Autowired
    private UserService userService;

    @Autowired
    private UserRepository userRepository;

    // Get current user profile
    @GetMapping("/me")
    public ResponseEntity<UserProfileDTO> getCurrentUser(Authentication auth) {
        User user = getUserFromAuth(auth);
        return ResponseEntity.ok(mapToProfileDTO(user));
    }

    // Get user statistics
    @GetMapping("/me/stats")
    public ResponseEntity<UserService.UserStats> getUserStats(Authentication auth) {
        User user = getUserFromAuth(auth);
        UserService.UserStats stats = userService.getUserStats(user.getId());
        return ResponseEntity.ok(stats);
    }

    // Get XP progress
    @GetMapping("/me/progress")
    public ResponseEntity<XPProgress> getXPProgress(Authentication auth) {
        User user = getUserFromAuth(auth);

        XPProgress progress = new XPProgress();
        progress.setCurrentXP(user.getExperiencePoints());
        progress.setCurrentLevel(user.getLevel());
        progress.setXpForNextLevel(userService.getXPForNextLevel(user.getId()));
        progress.setLevelProgress(userService.getLevelProgress(user.getId()));
        progress.setXpForCurrentLevel(userService.getXPForLevel(user.getLevel()));
        progress.setXpForNextLevelTotal(userService.getXPForLevel(user.getLevel() + 1));

        return ResponseEntity.ok(progress);
    }

    // Update profile
    @PutMapping("/me")
    public ResponseEntity<UserProfileDTO> updateProfile(
            Authentication auth,
            @RequestBody UpdateProfileRequest request) {
        User user = getUserFromAuth(auth);

        User updatedUser = userService.updateUserProfile(
                user.getId(),
                request.getFirstName(),
                request.getLastName(),
                request.getEmail()
        );

        return ResponseEntity.ok(mapToProfileDTO(updatedUser));
    }

    // Get streak info
    @GetMapping("/me/streak")
    public ResponseEntity<StreakInfo> getStreakInfo(Authentication auth) {
        User user = getUserFromAuth(auth);

        StreakInfo streak = new StreakInfo();
        streak.setCurrentStreak(user.getStreak());
        streak.setLongestStreak(user.getLongestStreak());
        streak.setBonusXP(calculateStreakBonus(user.getStreak()));

        return ResponseEntity.ok(streak);
    }

    // Helper methods
    private User getUserFromAuth(Authentication auth) {
        String username = auth.getName();
        return userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("User not found"));
    }

    private UserProfileDTO mapToProfileDTO(User user) {
        UserProfileDTO dto = new UserProfileDTO();
        dto.setId(user.getId());
        dto.setUsername(user.getUsername());
        dto.setEmail(user.getEmail());
        dto.setFirstName(user.getFirstName());
        dto.setLastName(user.getLastName());
        dto.setLevel(user.getLevel());
        dto.setExperiencePoints(user.getExperiencePoints());
        dto.setStreak(user.getStreak());
        dto.setTotalEventsCompleted(user.getTotalEventsCompleted());
        dto.setChallengesCompleted(user.getChallengesCompleted());
        return dto;
    }

    private int calculateStreakBonus(int streak) {
        // Bonus XP increases with streak length
        if (streak < 7) return 0;
        if (streak < 14) return 10;
        if (streak < 30) return 25;
        if (streak < 60) return 50;
        return 100;
    }

    // DTO Classes
    @lombok.Data
    public static class UserProfileDTO {
        private Long id;
        private String username;
        private String email;
        private String firstName;
        private String lastName;
        private int level;
        private int experiencePoints;
        private int streak;
        private int totalEventsCompleted;
        private int challengesCompleted;
    }

    @lombok.Data
    public static class UpdateProfileRequest {
        private String firstName;
        private String lastName;
        private String email;
    }

    @lombok.Data
    public static class XPProgress {
        private int currentXP;
        private int currentLevel;
        private int xpForNextLevel;
        private double levelProgress;
        private int xpForCurrentLevel;
        private int xpForNextLevelTotal;
    }

    @lombok.Data
    public static class StreakInfo {
        private int currentStreak;
        private int longestStreak;
        private int bonusXP;
    }
}
