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
import java.time.LocalTime;
import java.util.List;
import java.util.Optional;

@Service
@Transactional
public class EventService {

    @Autowired
    private EventRepository eventRepository;

    @Autowired
    private UserRepository userRepository;

    // Create new event
    public Event createEvent(Event event) {
        // Set default XP if not specified
        if (event.getXpReward() == null) {
            event.setXpReward(calculateXpReward(event));
        }
        return eventRepository.save(event);
    }

    // Update existing event
    public Event updateEvent(Long eventId, Event updatedEvent) {
        Event existing = eventRepository.findById(eventId)
                .orElseThrow(() -> new RuntimeException("Event not found"));

        existing.setTitle(updatedEvent.getTitle());
        existing.setDescription(updatedEvent.getDescription());
        existing.setStartTime(updatedEvent.getStartTime());
        existing.setEndTime(updatedEvent.getEndTime());
        existing.setLocation(updatedEvent.getLocation());
        existing.setCategory(updatedEvent.getCategory());
        existing.setColor(updatedEvent.getColor());
        existing.setReminder(updatedEvent.getReminder());
        existing.setReminderMinutes(updatedEvent.getReminderMinutes());

        return eventRepository.save(existing);
    }

    // Mark event as completed and award XP
    public Event completeEvent(Long eventId, Long userId) {
        Event event = eventRepository.findById(eventId)
                .orElseThrow(() -> new RuntimeException("Event not found"));

        if (!event.getUser().getId().equals(userId)) {
            throw new RuntimeException("Unauthorized");
        }

        if (!event.getCompleted()) {
            event.setCompleted(true);

            // Award XP to user
            User user = event.getUser();
            user.setExperiencePoints(user.getExperiencePoints() + event.getXpReward());

            // Check for level up
            int newLevel = calculateLevel(user.getExperiencePoints());
            if (newLevel > user.getLevel()) {
                user.setLevel(newLevel);
                // Could trigger level up notification here
            }

            userRepository.save(user);
            return eventRepository.save(event);
        }

        return event;
    }

    // Delete event
    public void deleteEvent(Long eventId, Long userId) {
        Event event = eventRepository.findById(eventId)
                .orElseThrow(() -> new RuntimeException("Event not found"));

        if (!event.getUser().getId().equals(userId)) {
            throw new RuntimeException("Unauthorized");
        }

        eventRepository.delete(event);
    }

    // Get events for a user
    public List<Event> getUserEvents(Long userId) {
        return eventRepository.findByUserId(userId);
    }

    // Get events for a specific date
    public List<Event> getEventsByDate(Long userId, LocalDate date) {
        LocalDateTime startOfDay = date.atStartOfDay();
        LocalDateTime endOfDay = date.atTime(LocalTime.MAX);
        return eventRepository.findByUserIdAndStartTimeBetween(userId, startOfDay, endOfDay);
    }

    // Get events for a date range (week/month view)
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

    // Get user statistics
    public EventStats getUserStats(Long userId) {
        Long completed = eventRepository.countByUserIdAndCompleted(userId, true);
        Long total = eventRepository.countByUserIdAndCompleted(userId, false) + completed;
        return new EventStats(total, completed);
    }

    // Calculate XP reward based on event duration/category
    private int calculateXpReward(Event event) {
        int baseXp = 10;

        // Bonus for longer events
        long duration = java.time.Duration.between(event.getStartTime(), event.getEndTime()).toHours();
        if (duration >= 2) baseXp += 5;
        if (duration >= 4) baseXp += 10;

        // Category bonuses
        if ("Work".equals(event.getCategory())) baseXp += 5;
        if ("Fitness".equals(event.getCategory())) baseXp += 10;
        if ("Learning".equals(event.getCategory())) baseXp += 8;

        return baseXp;
    }

    // Calculate level from XP (100 XP per level)
    private int calculateLevel(int xp) {
        return (xp / 100) + 1;
    }

    // Inner class for statistics
    public static class EventStats {
        public final Long totalEvents;
        public final Long completedEvents;

        public EventStats(Long total, Long completed) {
            this.totalEvents = total;
            this.completedEvents = completed;
        }
    }
}
