package com.nexora.core.content.service.impl;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import java.util.Optional;
import java.util.UUID;
import java.util.ArrayList;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.nexora.core.application.content.usecases.events.commands.ConfirmRSVPUseCase;
import com.nexora.core.domain.content.aggregates.UniversityEvent;
import com.nexora.core.domain.content.repositories.EventRepository;
import com.nexora.core.domain.user.aggregates.User;
import com.nexora.core.domain.user.repositories.UserRepository;

@ExtendWith(MockitoExtension.class)
class UniversityEventServiceImplTest {

    @Mock
    private EventRepository eventRepository;

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private ConfirmRSVPUseCase confirmRSVPUseCase;

    private UUID eventId;
    private UUID userId;
    private UniversityEvent event;
    private User user;

    @BeforeEach
    void setUp() {
        eventId = UUID.randomUUID();
        userId = UUID.randomUUID();

        event = new UniversityEvent();
        event.setId(eventId);
        event.setAttendeeIds(new ArrayList<>());

        user = new User();
        user.setId(userId);
    }

    @Test
    void confirmRSVP_Success() {
        when(eventRepository.findById(eventId)).thenReturn(Optional.of(event));
        when(userRepository.findById(userId)).thenReturn(Optional.of(user));
        when(eventRepository.save(any(UniversityEvent.class))).thenReturn(event);

        UniversityEvent updatedEvent = confirmRSVPUseCase.execute(eventId, userId);

        assertNotNull(updatedEvent);
        assertTrue(updatedEvent.getAttendeeIds().contains(userId));
        verify(eventRepository, times(1)).save(event);
    }

    @Test
    void confirmRSVP_AlreadyRegistered_ThrowsException() {
        event.getAttendeeIds().add(userId);
        when(eventRepository.findById(eventId)).thenReturn(Optional.of(event));
        when(userRepository.findById(userId)).thenReturn(Optional.of(user));

        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            confirmRSVPUseCase.execute(eventId, userId);
        });

        assertEquals("Ya estás registrado en este evento", exception.getMessage());
        verify(eventRepository, never()).save(any());
    }

    @Test
    void confirmRSVP_AlreadyRegistered_DifferentReferenceSameId_ThrowsException() {
        event.getAttendeeIds().add(userId);

        when(eventRepository.findById(eventId)).thenReturn(Optional.of(event));
        when(userRepository.findById(userId)).thenReturn(Optional.of(user));

        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            confirmRSVPUseCase.execute(eventId, userId);
        });

        assertEquals("Ya estás registrado en este evento", exception.getMessage());
        verify(eventRepository, never()).save(any());
    }

    @Test
    void confirmRSVP_EventNotFound_ThrowsException() {
        when(eventRepository.findById(eventId)).thenReturn(Optional.empty());

        assertThrows(RuntimeException.class, () -> {
            confirmRSVPUseCase.execute(eventId, userId);
        });
    }
}
