package com.example.kalenderbackend.service;

import com.example.kalenderbackend.entity.Event;
import com.example.kalenderbackend.entity.User;
import com.example.kalenderbackend.repository.EventRepository;
import com.example.kalenderbackend.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.List;

@Service
@Transactional
public class EventService {

    @Autowired
    private EventRepository eventRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private AchievementService achievementService;

    // Create new event
    public Event createEvent(Event event) {
        event.setCreatedAt(LocalDateTime.now());
        return eventRepository.save(event);
    }

    // Get all events for user
    public List<Event> getUserEvents(Long userId) {
        return eventRepository.findByUserId(userId);
    }

    // Get events by date
    public List<Event> getEventsByDate(Long userId, LocalDate date) {
        LocalDateTime startOfDay = date.atStartOfDay();
        LocalDateTime endOfDay = date.atTime(23, 59, 59);
        return eventRepository.findByUserIdAndStartTimeBetween(userId, startOfDay, endOfDay);
    }

    // Get events by date range
    public List<Event> getEventsByDateRange(Long userId, LocalDateTime start, LocalDateTime end) {
        return eventRepository.findByUserIdAndStartTimeBetween(userId, start, end);
    }

    // Get upcoming events
    public List<Event> getUpcomingEvents(Long userId) {
        return eventRepository.findByUserIdAndStartTimeAfterOrderByStartTimeAsc(userId, LocalDateTime.now());
    }

    // Get events by category
    public List<Event> getEventsByCategory(Long userId, String category) {
        return eventRepository.findByUserIdAndCategory(userId, category);
    }

    // Update event
    public Event updateEvent(Long eventId, Event updatedEvent) {
        Event existingEvent = eventRepository.findById(eventId)
                .orElseThrow(() -> new RuntimeException("Event not found"));

        existingEvent.setTitle(updatedEvent.getTitle());
        existingEvent.setDescription(updatedEvent.getDescription());
        existingEvent.setCategory(updatedEvent.getCategory());
        existingEvent.setStartTime(updatedEvent.getStartTime());
        existingEvent.setEndTime(updatedEvent.getEndTime());
        existingEvent.setAllDay(updatedEvent.isAllDay());
        existingEvent.setPriority(updatedEvent.getPriority());
        existingEvent.setRecurring(updatedEvent.isRecurring());
        existingEvent.setRecurrencePattern(updatedEvent.getRecurrencePattern());

        return eventRepository.save(existingEvent);
    }

    // Complete event and award XP
    public Event completeEvent(Long eventId, Long userId) {
        Event event = eventRepository.findByIdAndUserId(eventId, userId)
                .orElseThrow(() -> new RuntimeException("Event not found or unauthorized"));

        if (event.isCompleted()) {
            throw new RuntimeException("Event already completed");
        }

        event.setCompleted(true);
        event.setCompletedAt(LocalDateTime.now());

        // Calculate XP based on priority and category
        int baseXP = calculateXP(event);
        event.setXpReward(baseXP);

        // Update user stats
        User user = event.getUser();
        user.setXp(user.getXp() + baseXP);
        user.setTotalXP(user.getTotalXP() + baseXP);
        user.setEventsCompleted(user.getEventsCompleted() + 1);

        // Update streak
        updateUserStreak(user);

        // Check for level up
        checkAndUpdateLevel(user);

        // Save event and user
        eventRepository.save(event);
        userRepository.save(user);

        // Check for new achievements
        achievementService.checkAndUnlockAchievements(userId);

        return event;
    }

    // Calculate XP based on event properties
    private int calculateXP(Event event) {
        int baseXP = 10; // Base XP for completing any event

        // Add bonus based on category
        switch (event.getCategory().toUpperCase()) {
            case "WORK":
            case "STUDY":
                baseXP += 5;
                break;
            case "EXERCISE":
            case "HEALTH":
                baseXP += 10;
                break;
            case "SOCIAL":
                baseXP += 3;
                break;
            case "PERSONAL":
                baseXP += 2;
                break;
        }

        // Add bonus based on priority
        switch (event.getPriority()) {
            case HIGH:
                baseXP += 5;
                break;
            case MEDIUM:
                baseXP += 3;
                break;
            case LOW:
                baseXP += 1;
                break;
        }

        // Streak bonus
        User user = event.getUser();
        if (user.getCurrentStreakDays() >= 7) {
            baseXP = (int) (baseXP * 1.5); // 50% bonus for week streak
        } else if (user.getCurrentStreakDays() >= 3) {
            baseXP = (int) (baseXP * 1.25); // 25% bonus for 3-day streak
        }

        return baseXP;
    }

    // Update user's streak
    private void updateUserStreak(User user) {
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime lastEventDate = user.getLastEventDate();

        if (lastEventDate == null) {
            // First event ever
            user.setCurrentStreakDays(1);
            user.setStreakStartDate(now);
        } else {
            long daysBetween = ChronoUnit.DAYS.between(lastEventDate.toLocalDate(), now.toLocalDate());

            if (daysBetween == 0) {
                // Same day, streak continues
            } else if (daysBetween == 1) {
                // Next day, increase streak
                user.setCurrentStreakDays(user.getCurrentStreakDays() + 1);
            } else {
                // Streak broken, reset
                user.setCurrentStreakDays(1);
                user.setStreakStartDate(now);
            }
        }

        user.setLastEventDate(now);
    }

    // Check and update user level
    private void checkAndUpdateLevel(User user) {
        int currentXP = user.getXp();
        int currentLevel = user.getLevel();
        int xpForNextLevel = 100 * currentLevel; // XP needed for next level

        while (currentXP >= xpForNextLevel) {
            currentXP -= xpForNextLevel;
            currentLevel++;
            xpForNextLevel = 100 * currentLevel;
        }

        user.setLevel(currentLevel);
        user.setXp(currentXP);
    }

    // Delete event
    public void deleteEvent(Long eventId, Long userId) {
        Event event = eventRepository.findByIdAndUserId(eventId, userId)
                .orElseThrow(() -> new RuntimeException("Event not found or unauthorized"));

        // If event was completed, remove XP
        if (event.isCompleted()) {
            User user = event.getUser();
            user.setXp(Math.max(0, user.getXp() - event.getXpReward()));
            user.setEventsCompleted(Math.max(0, user.getEventsCompleted() - 1));
            userRepository.save(user);
        }

        eventRepository.delete(event);
    }

    // Get user statistics
    public EventStats getUserStats(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));

        EventStats stats = new EventStats();
        stats.totalEvents = eventRepository.countByUserId(userId);
        stats.completedEvents = eventRepository.countByUserIdAndCompleted(userId, true);
        stats.pendingEvents = eventRepository.countByUserIdAndCompleted(userId, false);
        stats.todayEvents = getEventsByDate(userId, LocalDate.now()).size();
        stats.currentStreak = user.getCurrentStreakDays();
        stats.level = user.getLevel();
        stats.currentXP = user.getXp();
        stats.totalXP = user.getTotalXP();
        stats.achievementsUnlocked = user.getAchievementsUnlocked();

        return stats;
    }

    // Statistics DTO
    public static class EventStats {
        public long totalEvents;
        public long completedEvents;
        public long pendingEvents;
        public int todayEvents;
        public int currentStreak;
        public int level;
        public int currentXP;
        public int totalXP;
        public int achievementsUnlocked;
    }
}
