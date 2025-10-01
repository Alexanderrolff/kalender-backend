package com.example.kalenderbackend.entity;

import jakarta.persistence.*;
import lombok.Data;
import java.time.LocalDateTime;

@Entity
@Table(name = "achievements")
@Data
public class Achievement {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(nullable = false)
    private String title;

    private String description;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private AchievementType achievementType;

    @Column(nullable = false)
    private int rewardXP;

    @Column(nullable = false)
    private boolean unlocked = false;

    private LocalDateTime unlockedAt;

    @Column(nullable = false)
    private LocalDateTime createdAt = LocalDateTime.now();

    // Achievement types - automatiska baserat på användarens beteende
    public enum AchievementType {
        FIRST_EVENT,           // Första eventet slutfört
        EVENTS_5,             // 5 events slutförda
        EVENTS_10,            // 10 events slutförda
        EVENTS_25,            // 25 events slutförda
        EVENTS_50,            // 50 events slutförda
        EVENTS_100,           // 100 events slutförda
        STREAK_3,             // 3 dagars streak
        STREAK_7,             // Veckostreak
        STREAK_14,            // 2 veckors streak
        STREAK_30,            // Månadsstreak
        LEVEL_5,              // Nått level 5
        LEVEL_10,             // Nått level 10
        LEVEL_25,             // Nått level 25
        PRODUCTIVE_WEEK,      // 7+ events på en vecka
        PRODUCTIVE_MONTH,     // 20+ events på en månad
        EARLY_BIRD,           // 5 events före kl 9
        NIGHT_OWL,            // 5 events efter kl 20
        WEEKEND_WARRIOR,      // 10 helg-events
        CATEGORY_MASTER       // 10 events i samma kategori
    }
}
