package com.example.kalenderbackend.controller;

import com.example.kalenderbackend.dto.CreateEventRequest;
import com.example.kalenderbackend.dto.EventDTO;
import com.example.kalenderbackend.entity.Event;
import com.example.kalenderbackend.entity.User;
import com.example.kalenderbackend.mapper.EventMapper;
import com.example.kalenderbackend.repository.UserRepository;
import com.example.kalenderbackend.service.EventService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/events")
@CrossOrigin(origins = "http://localhost:3000")
public class EventController {

    @Autowired
    private EventService eventService;

    @Autowired
    private EventMapper eventMapper;

    @Autowired
    private UserRepository userRepository;

    // Get all events for authenticated user
    @GetMapping
    public ResponseEntity<List<EventDTO>> getUserEvents(Authentication auth) {
        User user = getUserFromAuth(auth);
        List<Event> events = eventService.getUserEvents(user.getId());
        return ResponseEntity.ok(eventMapper.toDTOList(events));
    }

    // Get events by date
    @GetMapping("/date/{date}")
    public ResponseEntity<List<EventDTO>> getEventsByDate(
            Authentication auth,
            @PathVariable @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date) {
        User user = getUserFromAuth(auth);
        List<Event> events = eventService.getEventsByDate(user.getId(), date);
        return ResponseEntity.ok(eventMapper.toDTOList(events));
    }

    // Get events by date range
    @GetMapping("/range")
    public ResponseEntity<List<EventDTO>> getEventsByDateRange(
            Authentication auth,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime start,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime end) {
        User user = getUserFromAuth(auth);
        List<Event> events = eventService.getEventsByDateRange(user.getId(), start, end);
        return ResponseEntity.ok(eventMapper.toDTOList(events));
    }

    // Get upcoming events
    @GetMapping("/upcoming")
    public ResponseEntity<List<EventDTO>> getUpcomingEvents(Authentication auth) {
        User user = getUserFromAuth(auth);
        List<Event> events = eventService.getUpcomingEvents(user.getId());
        return ResponseEntity.ok(eventMapper.toDTOList(events));
    }

    // Get events by category
    @GetMapping("/category/{category}")
    public ResponseEntity<List<EventDTO>> getEventsByCategory(
            Authentication auth,
            @PathVariable String category) {
        User user = getUserFromAuth(auth);
        List<Event> events = eventService.getEventsByCategory(user.getId(), category);
        return ResponseEntity.ok(eventMapper.toDTOList(events));
    }

    // Create new event
    @PostMapping
    public ResponseEntity<EventDTO> createEvent(
            Authentication auth,
            @Valid @RequestBody CreateEventRequest request) {
        User user = getUserFromAuth(auth);
        Event event = eventMapper.toEntity(request, user);
        Event savedEvent = eventService.createEvent(event);
        return ResponseEntity.status(HttpStatus.CREATED).body(eventMapper.toDTO(savedEvent));
    }

    // Update event
    @PutMapping("/{id}")
    public ResponseEntity<EventDTO> updateEvent(
            Authentication auth,
            @PathVariable Long id,
            @Valid @RequestBody CreateEventRequest request) {
        User user = getUserFromAuth(auth);
        Event event = eventMapper.toEntity(request, user);
        Event updatedEvent = eventService.updateEvent(id, event);
        return ResponseEntity.ok(eventMapper.toDTO(updatedEvent));
    }

    // Complete event (award XP)
    @PostMapping("/{id}/complete")
    public ResponseEntity<EventDTO> completeEvent(
            Authentication auth,
            @PathVariable Long id) {
        User user = getUserFromAuth(auth);
        Event completedEvent = eventService.completeEvent(id, user.getId());
        return ResponseEntity.ok(eventMapper.toDTO(completedEvent));
    }

    // Delete event
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteEvent(
            Authentication auth,
            @PathVariable Long id) {
        User user = getUserFromAuth(auth);
        eventService.deleteEvent(id, user.getId());
        return ResponseEntity.noContent().build();
    }

    // Get user statistics
    @GetMapping("/stats")
    public ResponseEntity<EventService.EventStats> getUserStats(Authentication auth) {
        User user = getUserFromAuth(auth);
        EventService.EventStats stats = eventService.getUserStats(user.getId());
        return ResponseEntity.ok(stats);
    }

    // Helper method to get user from authentication
    private User getUserFromAuth(Authentication auth) {
        String username = auth.getName();
        return userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("User not found"));
    }
}
