package com.example.kalenderbackend.service;

import com.example.kalenderbackend.entity.Achievement;
import com.example.kalenderbackend.entity.User;
import com.example.kalenderbackend.repository.AchievementRepository;
import com.example.kalenderbackend.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
@Transactional
public class AchievementService {

    @Autowired
    private AchievementRepository achievementRepository;

    @Autowired
    private UserRepository userRepository;

    // Achievement definitions med XP-belöningar
    private static final Map<Achievement.AchievementType, AchievementDefinition> ACHIEVEMENT_DEFINITIONS = new HashMap<>();

    static {
        ACHIEVEMENT_DEFINITIONS.put(Achievement.AchievementType.FIRST_EVENT,
                new AchievementDefinition("Första steget!", "Du har slutfört ditt första event", 50));
        ACHIEVEMENT_DEFINITIONS.put(Achievement.AchievementType.EVENTS_5,
                new AchievementDefinition("På god väg", "5 events slutförda", 100));
        ACHIEVEMENT_DEFINITIONS.put(Achievement.AchievementType.EVENTS_10,
                new AchievementDefinition("Produktiv!", "10 events slutförda", 150));
        ACHIEVEMENT_DEFINITIONS.put(Achievement.AchievementType.EVENTS_25,
                new AchievementDefinition("Effektivitetsmästare", "25 events slutförda", 300));
        ACHIEVEMENT_DEFINITIONS.put(Achievement.AchievementType.EVENTS_50,
                new AchievementDefinition("Halvvägs till 100", "50 events slutförda", 500));
        ACHIEVEMENT_DEFINITIONS.put(Achievement.AchievementType.EVENTS_100,
                new AchievementDefinition("Centurion", "100 events slutförda!", 1000));
        ACHIEVEMENT_DEFINITIONS.put(Achievement.AchievementType.STREAK_3,
                new AchievementDefinition("Tre i rad", "3 dagars streak", 75));
        ACHIEVEMENT_DEFINITIONS.put(Achievement.AchievementType.STREAK_7,
                new AchievementDefinition("Veckokrigare", "7 dagars streak", 200));
        ACHIEVEMENT_DEFINITIONS.put(Achievement.AchievementType.STREAK_14,
                new AchievementDefinition("Två veckor stark", "14 dagars streak", 400));
        ACHIEVEMENT_DEFINITIONS.put(Achievement.AchievementType.STREAK_30,
                new AchievementDefinition("Månadshjälte", "30 dagars streak!", 800));
        ACHIEVEMENT_DEFINITIONS.put(Achievement.AchievementType.LEVEL_5,
                new AchievementDefinition("Nybörjare", "Nått level 5", 100));
        ACHIEVEMENT_DEFINITIONS.put(Achievement.AchievementType.LEVEL_10,
                new AchievementDefinition("Erfaren", "Nått level 10", 250));
        ACHIEVEMENT_DEFINITIONS.put(Achievement.AchievementType.LEVEL_25,
                new AchievementDefinition("Veteran", "Nått level 25", 500));
        ACHIEVEMENT_DEFINITIONS.put(Achievement.AchievementType.PRODUCTIVE_WEEK,
                new AchievementDefinition("Produktiv vecka", "7+ events på en vecka", 150));
        ACHIEVEMENT_DEFINITIONS.put(Achievement.AchievementType.PRODUCTIVE_MONTH,
                new AchievementDefinition("Produktiv månad", "20+ events på en månad", 400));
        ACHIEVEMENT_DEFINITIONS.put(Achievement.AchievementType.EARLY_BIRD,
                new AchievementDefinition("Tidigt uppe", "5 events före kl 9", 100));
        ACHIEVEMENT_DEFINITIONS.put(Achievement.AchievementType.NIGHT_OWL,
                new AchievementDefinition("Nattuggla", "5 events efter kl 20", 100));
        ACHIEVEMENT_DEFINITIONS.put(Achievement.AchievementType.WEEKEND_WARRIOR,
                new AchievementDefinition("Helgkrigare", "10 events på helger", 150));
        ACHIEVEMENT_DEFINITIONS.put(Achievement.AchievementType.CATEGORY_MASTER,
                new AchievementDefinition("Kategorimästare", "10 events i samma kategori", 125));
    }

    // Check och unlock achievements baserat på användarens statistik
    public void checkAndUnlockAchievements(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));

        // Check event-based achievements
        checkEventAchievements(user);

        // Check streak achievements
        checkStreakAchievements(user);

        // Check level achievements
        checkLevelAchievements(user);
    }

    private void checkEventAchievements(User user) {
        int eventsCompleted = user.getEventsCompleted();

        if (eventsCompleted >= 1) {
            unlockAchievement(user, Achievement.AchievementType.FIRST_EVENT);
        }
        if (eventsCompleted >= 5) {
            unlockAchievement(user, Achievement.AchievementType.EVENTS_5);
        }
        if (eventsCompleted >= 10) {
            unlockAchievement(user, Achievement.AchievementType.EVENTS_10);
        }
        if (eventsCompleted >= 25) {
            unlockAchievement(user, Achievement.AchievementType.EVENTS_25);
        }
        if (eventsCompleted >= 50) {
            unlockAchievement(user, Achievement.AchievementType.EVENTS_50);
        }
        if (eventsCompleted >= 100) {
            unlockAchievement(user, Achievement.AchievementType.EVENTS_100);
        }
    }

    private void checkStreakAchievements(User user) {
        int streak = user.getCurrentStreakDays();

        if (streak >= 3) {
            unlockAchievement(user, Achievement.AchievementType.STREAK_3);
        }
        if (streak >= 7) {
            unlockAchievement(user, Achievement.AchievementType.STREAK_7);
        }
        if (streak >= 14) {
            unlockAchievement(user, Achievement.AchievementType.STREAK_14);
        }
        if (streak >= 30) {
            unlockAchievement(user, Achievement.AchievementType.STREAK_30);
        }
    }

    private void checkLevelAchievements(User user) {
        int level = user.getLevel();

        if (level >= 5) {
            unlockAchievement(user, Achievement.AchievementType.LEVEL_5);
        }
        if (level >= 10) {
            unlockAchievement(user, Achievement.AchievementType.LEVEL_10);
        }
        if (level >= 25) {
            unlockAchievement(user, Achievement.AchievementType.LEVEL_25);
        }
    }

    private void unlockAchievement(User user, Achievement.AchievementType type) {
        // Check om achievement redan är unlocked
        if (achievementRepository.existsByUserIdAndAchievementType(user.getId(), type)) {
            return;
        }

        AchievementDefinition definition = ACHIEVEMENT_DEFINITIONS.get(type);

        Achievement achievement = new Achievement();
        achievement.setUser(user);
        achievement.setTitle(definition.title);
        achievement.setDescription(definition.description);
        achievement.setAchievementType(type);
        achievement.setRewardXP(definition.rewardXP);
        achievement.setUnlocked(true);
        achievement.setUnlockedAt(LocalDateTime.now());

        achievementRepository.save(achievement);

        // Ge XP till användaren
        user.setXp(user.getXp() + definition.rewardXP);
        user.setTotalXP(user.getTotalXP() + definition.rewardXP);
        user.setAchievementsUnlocked(user.getAchievementsUnlocked() + 1);

        // Check level up
        checkLevelUp(user);

        userRepository.save(user);
    }

    private void checkLevelUp(User user) {
        int currentXP = user.getXp();
        int currentLevel = user.getLevel();
        int xpForNextLevel = 100 * currentLevel;

        while (currentXP >= xpForNextLevel) {
            currentXP -= xpForNextLevel;
            currentLevel++;
            xpForNextLevel = 100 * currentLevel;
        }

        user.setLevel(currentLevel);
        user.setXp(currentXP);
    }

    // Get user's achievements
    public List<Achievement> getUserAchievements(Long userId) {
        return achievementRepository.findByUserId(userId);
    }

    public List<Achievement> getUnlockedAchievements(Long userId) {
        return achievementRepository.findByUserIdAndUnlocked(userId, true);
    }

    public List<Achievement> getLockedAchievements(Long userId) {
        return achievementRepository.findByUserIdAndUnlocked(userId, false);
    }

    // Helper class for achievement definitions
    private static class AchievementDefinition {
        final String title;
        final String description;
        final int rewardXP;

        AchievementDefinition(String title, String description, int rewardXP) {
            this.title = title;
            this.description = description;
            this.rewardXP = rewardXP;
        }
    }
}
