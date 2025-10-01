package com.example.kalenderbackend.mapper;

import com.example.kalenderbackend.dto.CreateEventRequest;
import com.example.kalenderbackend.dto.EventDTO;
import com.example.kalenderbackend.entity.Event;
import com.example.kalenderbackend.entity.User;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.stream.Collectors;

@Component
public class EventMapper {

    // Convert Event entity to EventDTO
    public EventDTO toDTO(Event event) {
        if (event == null) {
            return null;
        }

        EventDTO dto = new EventDTO();
        dto.setId(event.getId());
        dto.setTitle(event.getTitle());
        dto.setDescription(event.getDescription());
        dto.setStartTime(event.getStartTime());
        dto.setEndTime(event.getEndTime());
        dto.setLocation(event.getLocation());
        dto.setCategory(event.getCategory());
        dto.setColor(event.getColor());
        dto.setCompleted(event.getCompleted());
        dto.setXpReward(event.getXpReward());
        dto.setReminder(event.getReminder());
        dto.setReminderMinutes(event.getReminderMinutes());

        if (event.getUser() != null) {
            dto.setUserId(event.getUser().getId());
            dto.setUserName(event.getUser().getUsername());
        }

        dto.setCreatedAt(event.getCreatedAt());
        dto.setUpdatedAt(event.getUpdatedAt());

        return dto;
    }

    // Convert list of Events to list of EventDTOs
    public List<EventDTO> toDTOList(List<Event> events) {
        return events.stream()
                .map(this::toDTO)
                .collect(Collectors.toList());
    }

    // Convert CreateEventRequest to Event entity
    public Event toEntity(CreateEventRequest request, User user) {
        if (request == null) {
            return null;
        }

        Event event = new Event();
        event.setTitle(request.getTitle());
        event.setDescription(request.getDescription());
        event.setStartTime(request.getStartTime());
        event.setEndTime(request.getEndTime());
        event.setLocation(request.getLocation());
        event.setCategory(request.getCategory());
        event.setColor(request.getColor());
        event.setXpReward(request.getXpReward());
        event.setReminder(request.getReminder());
        event.setReminderMinutes(request.getReminderMinutes());
        event.setUser(user);
        event.setCompleted(false);

        return event;
    }

    // Update existing entity from request
    public void updateEntityFromRequest(Event event, CreateEventRequest request) {
        event.setTitle(request.getTitle());
        event.setDescription(request.getDescription());
        event.setStartTime(request.getStartTime());
        event.setEndTime(request.getEndTime());
        event.setLocation(request.getLocation());
        event.setCategory(request.getCategory());
        event.setColor(request.getColor());
        event.setXpReward(request.getXpReward());
        event.setReminder(request.getReminder());
        event.setReminderMinutes(request.getReminderMinutes());
    }
}
