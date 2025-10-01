package com.example.kalenderbackend.repository;

import com.example.kalenderbackend.entity.Event;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface EventRepository extends JpaRepository<Event, Long> {

    // Find events for a specific user
    List<Event> findByUserId(Long userId);

    // Find events for a user within a date range
    List<Event> findByUserIdAndStartTimeBetween(Long userId, LocalDateTime start, LocalDateTime end);

    // Find upcoming events for a user
    List<Event> findByUserIdAndStartTimeAfterOrderByStartTimeAsc(Long userId, LocalDateTime after);

    // Find events by category for a user
    List<Event> findByUserIdAndCategory(Long userId, String category);

    // Find completed events for a user
    List<Event> findByUserIdAndCompleted(Long userId, Boolean completed);

    // Custom query for events that need reminders
    @Query("SELECT e FROM Event e WHERE e.reminder = true AND e.completed = false " +
            "AND e.startTime BETWEEN :now AND :reminderTime")
    List<Event> findEventsNeedingReminders(@Param("now") LocalDateTime now,
                                           @Param("reminderTime") LocalDateTime reminderTime);

    // Count events for statistics
    Long countByUserIdAndCompleted(Long userId, Boolean completed);

    // Find events for a specific date
    @Query("SELECT e FROM Event e WHERE e.user.id = :userId " +
            "AND DATE(e.startTime) = DATE(:date)")
    List<Event> findByUserIdAndDate(@Param("userId") Long userId,
                                    @Param("date") LocalDateTime date);
}
