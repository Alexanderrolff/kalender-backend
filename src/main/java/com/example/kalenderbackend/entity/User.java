package com.example.kalenderbackend.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;
import java.util.List;
import java.util.ArrayList;

@Entity
@Table(name = "users")
public class User {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true, nullable = false)
    private String email;

    @Column(nullable = false)
    private String username;

    @Column(nullable = false)
    private String password;

    private Integer level = 1;
    private Integer xp = 0;
    private Integer streak = 0;
    private Integer achievementsUnlocked = 0;
    private Integer totalXP = 0;
    private Integer eventsCompleted = 0;
    private LocalDateTime lastEventDate;
    private Integer currentStreakDays = 0;
    private LocalDateTime streakStartDate;

    @Column(name = "created_at")
    private LocalDateTime createdAt = LocalDateTime.now();

    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL)
    private List<Event> events = new ArrayList<>();

    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL)
    private List<Achievement> achievements = new ArrayList<>();

    // Getters och setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }

    public String getPassword() { return password; }
    public void setPassword(String password) { this.password = password; }

    public Integer getLevel() { return level; }
    public void setLevel(Integer level) { this.level = level; }

    public Integer getXp() { return xp; }
    public void setXp(Integer xp) { this.xp = xp; }

    public Integer getStreak() { return streak; }
    public void setStreak(Integer streak) { this.streak = streak; }

    public Integer getAchievementsUnlocked() { return achievementsUnlocked; }
    public void setAchievementsUnlocked(Integer achievementsUnlocked) { this.achievementsUnlocked = achievementsUnlocked; }

    public Integer getTotalXP() { return totalXP; }
    public void setTotalXP(Integer totalXP) { this.totalXP = totalXP; }

    public Integer getEventsCompleted() { return eventsCompleted; }
    public void setEventsCompleted(Integer eventsCompleted) { this.eventsCompleted = eventsCompleted; }

    public LocalDateTime getLastEventDate() { return lastEventDate; }
    public void setLastEventDate(LocalDateTime lastEventDate) { this.lastEventDate = lastEventDate; }

    public Integer getCurrentStreakDays() { return currentStreakDays; }
    public void setCurrentStreakDays(Integer currentStreakDays) { this.currentStreakDays = currentStreakDays; }

    public LocalDateTime getStreakStartDate() { return streakStartDate; }
    public void setStreakStartDate(LocalDateTime streakStartDate) { this.streakStartDate = streakStartDate; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

    public List<Event> getEvents() { return events; }
    public void setEvents(List<Event> events) { this.events = events; }

    public List<Achievement> getAchievements() { return achievements; }
    public void setAchievements(List<Achievement> achievements) { this.achievements = achievements; }
}
