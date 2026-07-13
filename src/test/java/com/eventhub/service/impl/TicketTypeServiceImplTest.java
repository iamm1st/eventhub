package com.eventhub.service.impl;

import com.eventhub.dto.request.TicketTypeCreateRequest;
import com.eventhub.dto.request.TicketTypeUpdateRequest;
import com.eventhub.dto.response.TicketTypeResponse;
import com.eventhub.entity.Event;
import com.eventhub.entity.TicketType;
import com.eventhub.entity.User;
import com.eventhub.enums.RegistrationStatus;
import com.eventhub.enums.RoleName;
import com.eventhub.exception.ticket.TicketQuantityBelowSoldException;
import com.eventhub.exception.ticket.TicketQuantityExceededException;
import com.eventhub.exception.ticket.TicketTypeAlreadyExistsException;
import com.eventhub.exception.ticket.TicketTypeInUseException;
import com.eventhub.mapper.TicketTypeMapper;
import com.eventhub.repository.EventRepository;
import com.eventhub.repository.RegistrationRepository;
import com.eventhub.repository.TicketTypeRepository;
import com.eventhub.security.CurrentUserProvider;
import com.eventhub.support.TestDataFactory;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class TicketTypeServiceImplTest {

    @Mock
    private TicketTypeRepository ticketTypeRepository;

    @Mock
    private EventRepository eventRepository;

    @Mock
    private RegistrationRepository registrationRepository;

    @Mock
    private CurrentUserProvider currentUserProvider;

    @Mock
    private TicketTypeMapper ticketTypeMapper;

    @InjectMocks
    private TicketTypeServiceImpl ticketTypeService;

    @Test
    void createTicketTypeShouldCreateTicketTypeForEventOwner() {
        User organizer = TestDataFactory.user(1L, RoleName.ROLE_ORGANIZER);
        Event event = TestDataFactory.futurePublishedEvent(10L, organizer);

        TicketTypeCreateRequest request = TicketTypeCreateRequest.builder()
                .name(" Standard ")
                .price(BigDecimal.valueOf(50))
                .totalQuantity(20)
                .build();

        TicketTypeResponse expectedResponse = TicketTypeResponse.builder()
                .id(100L)
                .eventId(event.getId())
                .name("Standard")
                .totalQuantity(20)
                .availableQuantity(20)
                .build();

        when(eventRepository.findById(event.getId())).thenReturn(Optional.of(event));
        when(currentUserProvider.getCurrentUserId()).thenReturn(organizer.getId());
        when(ticketTypeRepository.existsByEventIdAndNameIgnoreCase(event.getId(), "Standard")).thenReturn(false);
        when(ticketTypeRepository.sumTotalQuantityByEventId(event.getId())).thenReturn(0L);
        when(ticketTypeRepository.save(any(TicketType.class))).thenAnswer(invocation -> {
            TicketType ticketType = invocation.getArgument(0);
            ticketType.setId(100L);
            return ticketType;
        });
        when(ticketTypeMapper.toResponse(any(TicketType.class))).thenReturn(expectedResponse);

        TicketTypeResponse actualResponse = ticketTypeService.createTicketType(event.getId(), request);

        assertEquals(expectedResponse, actualResponse);
        verify(ticketTypeRepository).save(any(TicketType.class));
    }

    @Test
    void createTicketTypeShouldThrowExceptionWhenNameAlreadyExists() {
        User organizer = TestDataFactory.user(1L, RoleName.ROLE_ORGANIZER);
        Event event = TestDataFactory.futurePublishedEvent(10L, organizer);

        TicketTypeCreateRequest request = TicketTypeCreateRequest.builder()
                .name("Standard")
                .price(BigDecimal.valueOf(50))
                .totalQuantity(20)
                .build();

        when(eventRepository.findById(event.getId())).thenReturn(Optional.of(event));
        when(currentUserProvider.getCurrentUserId()).thenReturn(organizer.getId());
        when(ticketTypeRepository.existsByEventIdAndNameIgnoreCase(event.getId(), "Standard")).thenReturn(true);

        assertThrows(TicketTypeAlreadyExistsException.class, () -> ticketTypeService.createTicketType(event.getId(), request));

        verify(ticketTypeRepository, never()).save(any(TicketType.class));
    }

    @Test
    void createTicketTypeShouldThrowExceptionWhenQuantityExceedsCapacity() {
        User organizer = TestDataFactory.user(1L, RoleName.ROLE_ORGANIZER);
        Event event = TestDataFactory.futurePublishedEvent(10L, organizer);
        event.setCapacity(30);

        TicketTypeCreateRequest request = TicketTypeCreateRequest.builder()
                .name("VIP")
                .price(BigDecimal.valueOf(100))
                .totalQuantity(20)
                .build();

        when(eventRepository.findById(event.getId())).thenReturn(Optional.of(event));
        when(currentUserProvider.getCurrentUserId()).thenReturn(organizer.getId());
        when(ticketTypeRepository.existsByEventIdAndNameIgnoreCase(event.getId(), "VIP")).thenReturn(false);
        when(ticketTypeRepository.sumTotalQuantityByEventId(event.getId())).thenReturn(20L);

        assertThrows(TicketQuantityExceededException.class, () -> ticketTypeService.createTicketType(event.getId(), request));

        verify(ticketTypeRepository, never()).save(any(TicketType.class));
    }

    @Test
    void updateTicketTypeShouldUpdateTicketDataAndAvailableQuantity() {
        User organizer = TestDataFactory.user(1L, RoleName.ROLE_ORGANIZER);
        Event event = TestDataFactory.futurePublishedEvent(10L, organizer);
        TicketType ticketType = TestDataFactory.ticketType(100L, event, 8);
        ticketType.setTotalQuantity(10);

        TicketTypeUpdateRequest request = TicketTypeUpdateRequest.builder()
                .name(" Standard Plus ")
                .price(BigDecimal.valueOf(70))
                .totalQuantity(15)
                .build();

        TicketTypeResponse expectedResponse = TicketTypeResponse.builder()
                .id(ticketType.getId())
                .name("Standard Plus")
                .totalQuantity(15)
                .availableQuantity(13)
                .build();

        when(ticketTypeRepository.findById(ticketType.getId())).thenReturn(Optional.of(ticketType));
        when(currentUserProvider.getCurrentUserId()).thenReturn(organizer.getId());
        when(ticketTypeRepository.existsByEventIdAndNameIgnoreCaseAndIdNot(
                event.getId(),
                "Standard Plus",
                ticketType.getId())).thenReturn(false);
        when(ticketTypeRepository.sumTotalQuantityByEventIdExcludingTicketType(event.getId(), ticketType.getId()))
                .thenReturn(0L);
        when(ticketTypeMapper.toResponse(ticketType)).thenReturn(expectedResponse);

        TicketTypeResponse actualResponse = ticketTypeService.updateTicketType(ticketType.getId(), request);

        assertEquals(expectedResponse, actualResponse);
        assertEquals("Standard Plus", ticketType.getName());
        assertEquals(15, ticketType.getTotalQuantity());
        assertEquals(13, ticketType.getAvailableQuantity());
    }

    @Test
    void updateTicketTypeShouldThrowExceptionWhenNewQuantityIsBelowSoldQuantity() {
        User organizer = TestDataFactory.user(1L, RoleName.ROLE_ORGANIZER);
        Event event = TestDataFactory.futurePublishedEvent(10L, organizer);
        TicketType ticketType = TestDataFactory.ticketType(100L, event, 3);
        ticketType.setTotalQuantity(10);

        TicketTypeUpdateRequest request = TicketTypeUpdateRequest.builder()
                .name("Standard")
                .price(BigDecimal.valueOf(50))
                .totalQuantity(5)
                .build();

        when(ticketTypeRepository.findById(ticketType.getId())).thenReturn(Optional.of(ticketType));
        when(currentUserProvider.getCurrentUserId()).thenReturn(organizer.getId());
        when(ticketTypeRepository.existsByEventIdAndNameIgnoreCaseAndIdNot(
                event.getId(),
                "Standard",
                ticketType.getId())).thenReturn(false);

        assertThrows(TicketQuantityBelowSoldException.class, () -> ticketTypeService.updateTicketType(ticketType.getId(), request));
    }

    @Test
    void deleteTicketTypeShouldThrowExceptionWhenItHasActiveRegistrations() {
        User organizer = TestDataFactory.user(1L, RoleName.ROLE_ORGANIZER);
        Event event = TestDataFactory.futurePublishedEvent(10L, organizer);
        TicketType ticketType = TestDataFactory.ticketType(100L, event, 10);

        when(ticketTypeRepository.findById(ticketType.getId())).thenReturn(Optional.of(ticketType));
        when(currentUserProvider.getCurrentUserId()).thenReturn(organizer.getId());
        when(registrationRepository.existsByTicketTypeIdAndStatus(ticketType.getId(), RegistrationStatus.ACTIVE)).thenReturn(true);

        assertThrows(TicketTypeInUseException.class, () -> ticketTypeService.deleteTicketType(ticketType.getId()));

        verify(ticketTypeRepository, never()).delete(ticketType);
    }
}