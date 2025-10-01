package com.example.kalenderbackend.controller;

import com.example.kalenderbackend.entity.Achievement;
import com.example.kalenderbackend.entity.User;
import com.example.kalenderbackend.repository.UserRepository;
import com.example.kalenderbackend.service.AchievementService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/achievements")
@CrossOrigin(origins = "http://localhost:3000")
public class AchievementController {

    @Autowired
    private AchievementService achievementService;

    @Autowired
    private UserRepository userRepository;

    // Get all achievements for user
    @GetMapping
    public ResponseEntity<List<AchievementDTO>> getUserAchievements(Authentication auth) {
        User user = getUserFromAuth(auth);
        List<Achievement> achievements = achievementService.getUserAchievements(user.getId());

        List<AchievementDTO> dtos = achievements.stream()
                .map(this::toDTO)
                .collect(Collectors.toList());

        return ResponseEntity.ok(dtos);
    }

    // Get only unlocked achievements
    @GetMapping("/unlocked")
    public ResponseEntity<List<AchievementDTO>> getUnlockedAchievements(Authentication auth) {
        User user = getUserFromAuth(auth);
        List<Achievement> achievements = achievementService.getUnlockedAchievements(user.getId());

        List<AchievementDTO> dtos = achievements.stream()
                .map(this::toDTO)
                .collect(Collectors.toList());

        return ResponseEntity.ok(dtos);
    }

    // Get locked achievements (to show what's available)
    @GetMapping("/available")
    public ResponseEntity<List<AchievementDTO>> getAvailableAchievements(Authentication auth) {
        User user = getUserFromAuth(auth);

        // Get all possible achievement types
        List<AchievementDTO> allAchievements = Achievement.AchievementType.values().length > 0 ?
                java.util.Arrays.stream(Achievement.AchievementType.values())
                        .map(type -> {
                            AchievementDTO dto = new AchievementDTO();
                            dto.type = type.name();
                            dto.title = getAchievementTitle(type);
                            dto.description = getAchievementDescription(type);
                            dto.rewardXP = getAchievementXP(type);
                            dto.unlocked = achievementService.getUserAchievements(user.getId()).stream()
                                    .anyMatch(a -> a.getAchievementType() == type && a.isUnlocked());
                            return dto;
                        })
                        .collect(Collectors.toList()) : new java.util.ArrayList<>();

        return ResponseEntity.ok(allAchievements);
    }

    // Check for new achievements (can be called after events)
    @PostMapping("/check")
    public ResponseEntity<Map<String, Object>> checkForNewAchievements(Authentication auth) {
        User user = getUserFromAuth(auth);

        int beforeCount = achievementService.getUnlockedAchievements(user.getId()).size();
        achievementService.checkAndUnlockAchievements(user.getId());
        int afterCount = achievementService.getUnlockedAchievements(user.getId()).size();

        Map<String, Object> response = new HashMap<>();
        response.put("newAchievements", afterCount - beforeCount);
        response.put("totalUnlocked", afterCount);

        return ResponseEntity.ok(response);
    }

    // Get achievement statistics
    @GetMapping("/stats")
    public ResponseEntity<Map<String, Object>> getAchievementStats(Authentication auth) {
        User user = getUserFromAuth(auth);
        List<Achievement> unlocked = achievementService.getUnlockedAchievements(user.getId());

        Map<String, Object> stats = new HashMap<>();
        stats.put("totalUnlocked", unlocked.size());
        stats.put("totalAvailable", Achievement.AchievementType.values().length);
        stats.put("totalXPFromAchievements", unlocked.stream()
                .mapToInt(Achievement::getRewardXP)
                .sum());
        stats.put("completionPercentage",
                (unlocked.size() * 100.0) / Achievement.AchievementType.values().length);

        return ResponseEntity.ok(stats);
    }

    // Get recent achievements
    @GetMapping("/recent")
    public ResponseEntity<List<AchievementDTO>> getRecentAchievements(
            Authentication auth,
            @RequestParam(defaultValue = "5") int limit) {
        User user = getUserFromAuth(auth);
        List<Achievement> achievements = achievementService.getUnlockedAchievements(user.getId());

        List<AchievementDTO> recentAchievements = achievements.stream()
                .filter(Achievement::isUnlocked)
                .sorted((a, b) -> b.getUnlockedAt().compareTo(a.getUnlockedAt()))
                .limit(limit)
                .map(this::toDTO)
                .collect(Collectors.toList());

        return ResponseEntity.ok(recentAchievements);
    }

    // Helper method to convert Achievement to DTO
    private AchievementDTO toDTO(Achievement achievement) {
        AchievementDTO dto = new AchievementDTO();
        dto.id = achievement.getId();
        dto.title = achievement.getTitle();
        dto.description = achievement.getDescription();
        dto.type = achievement.getAchievementType().name();
        dto.rewardXP = achievement.getRewardXP();
        dto.unlocked = achievement.isUnlocked();
        dto.unlockedAt = achievement.getUnlockedAt();
        return dto;
    }

    // Helper methods for achievement metadata
    private String getAchievementTitle(Achievement.AchievementType type) {
        Map<Achievement.AchievementType, String> titles = Map.ofEntries(
                Map.entry(Achievement.AchievementType.FIRST_EVENT, "Första steget!"),
                Map.entry(Achievement.AchievementType.EVENTS_5, "På god väg"),
                Map.entry(Achievement.AchievementType.EVENTS_10, "Produktiv!"),
                Map.entry(Achievement.AchievementType.EVENTS_25, "Effektivitetsmästare"),
                Map.entry(Achievement.AchievementType.EVENTS_50, "Halvvägs till 100"),
                Map.entry(Achievement.AchievementType.EVENTS_100, "Centurion"),
                Map.entry(Achievement.AchievementType.STREAK_3, "Tre i rad"),
                Map.entry(Achievement.AchievementType.STREAK_7, "Veckokrigare"),
                Map.entry(Achievement.AchievementType.STREAK_14, "Två veckor stark"),
                Map.entry(Achievement.AchievementType.STREAK_30, "Månadshjälte"),
                Map.entry(Achievement.AchievementType.LEVEL_5, "Nybörjare"),
                Map.entry(Achievement.AchievementType.LEVEL_10, "Erfaren"),
                Map.entry(Achievement.AchievementType.LEVEL_25, "Veteran"),
                Map.entry(Achievement.AchievementType.PRODUCTIVE_WEEK, "Produktiv vecka"),
                Map.entry(Achievement.AchievementType.PRODUCTIVE_MONTH, "Produktiv månad"),
                Map.entry(Achievement.AchievementType.EARLY_BIRD, "Tidigt uppe"),
                Map.entry(Achievement.AchievementType.NIGHT_OWL, "Nattuggla"),
                Map.entry(Achievement.AchievementType.WEEKEND_WARRIOR, "Helgkrigare"),
                Map.entry(Achievement.AchievementType.CATEGORY_MASTER, "Kategorimästare")
        );
        return titles.getOrDefault(type, "Unknown Achievement");
    }

    private String getAchievementDescription(Achievement.AchievementType type) {
        Map<Achievement.AchievementType, String> descriptions = Map.ofEntries(
                Map.entry(Achievement.AchievementType.FIRST_EVENT, "Slutför ditt första event"),
                Map.entry(Achievement.AchievementType.EVENTS_5, "Slutför 5 events"),
                Map.entry(Achievement.AchievementType.EVENTS_10, "Slutför 10 events"),
                Map.entry(Achievement.AchievementType.EVENTS_25, "Slutför 25 events"),
                Map.entry(Achievement.AchievementType.EVENTS_50, "Slutför 50 events"),
                Map.entry(Achievement.AchievementType.EVENTS_100, "Slutför 100 events"),
                Map.entry(Achievement.AchievementType.STREAK_3, "Håll en 3-dagars streak"),
                Map.entry(Achievement.AchievementType.STREAK_7, "Håll en 7-dagars streak"),
                Map.entry(Achievement.AchievementType.STREAK_14, "Håll en 14-dagars streak"),
                Map.entry(Achievement.AchievementType.STREAK_30, "Håll en 30-dagars streak"),
                Map.entry(Achievement.AchievementType.LEVEL_5, "Nå level 5"),
                Map.entry(Achievement.AchievementType.LEVEL_10, "Nå level 10"),
                Map.entry(Achievement.AchievementType.LEVEL_25, "Nå level 25"),
                Map.entry(Achievement.AchievementType.PRODUCTIVE_WEEK, "Slutför 7+ events på en vecka"),
                Map.entry(Achievement.AchievementType.PRODUCTIVE_MONTH, "Slutför 20+ events på en månad"),
                Map.entry(Achievement.AchievementType.EARLY_BIRD, "Slutför 5 events före kl 9"),
                Map.entry(Achievement.AchievementType.NIGHT_OWL, "Slutför 5 events efter kl 20"),
                Map.entry(Achievement.AchievementType.WEEKEND_WARRIOR, "Slutför 10 events på helger"),
                Map.entry(Achievement.AchievementType.CATEGORY_MASTER, "Slutför 10 events i samma kategori")
        );
        return descriptions.getOrDefault(type, "Complete the achievement");
    }

    private int getAchievementXP(Achievement.AchievementType type) {
        Map<Achievement.AchievementType, Integer> xpRewards = Map.ofEntries(
                Map.entry(Achievement.AchievementType.FIRST_EVENT, 50),
                Map.entry(Achievement.AchievementType.EVENTS_5, 100),
                Map.entry(Achievement.AchievementType.EVENTS_10, 150),
                Map.entry(Achievement.AchievementType.EVENTS_25, 300),
                Map.entry(Achievement.AchievementType.EVENTS_50, 500),
                Map.entry(Achievement.AchievementType.EVENTS_100, 1000),
                Map.entry(Achievement.AchievementType.STREAK_3, 75),
                Map.entry(Achievement.AchievementType.STREAK_7, 200),
                Map.entry(Achievement.AchievementType.STREAK_14, 400),
                Map.entry(Achievement.AchievementType.STREAK_30, 800),
                Map.entry(Achievement.AchievementType.LEVEL_5, 100),
                Map.entry(Achievement.AchievementType.LEVEL_10, 250),
                Map.entry(Achievement.AchievementType.LEVEL_25, 500),
                Map.entry(Achievement.AchievementType.PRODUCTIVE_WEEK, 150),
                Map.entry(Achievement.AchievementType.PRODUCTIVE_MONTH, 400),
                Map.entry(Achievement.AchievementType.EARLY_BIRD, 100),
                Map.entry(Achievement.AchievementType.NIGHT_OWL, 100),
                Map.entry(Achievement.AchievementType.WEEKEND_WARRIOR, 150),
                Map.entry(Achievement.AchievementType.CATEGORY_MASTER, 125)
        );
        return xpRewards.getOrDefault(type, 50);
    }

    // Helper method to get user from authentication
    private User getUserFromAuth(Authentication auth) {
        String username = auth.getName();
        return userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("User not found"));
    }

    // DTO for Achievement
    public static class AchievementDTO {
        public Long id;
        public String title;
        public String description;
        public String type;
        public int rewardXP;
        public boolean unlocked;
        public java.time.LocalDateTime unlockedAt;
    }
}
