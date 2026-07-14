package com.eventhub.exception;

import com.eventhub.enums.RoleName;
import com.eventhub.exception.auth.EmailAlreadyExistsException;
import com.eventhub.exception.auth.InvalidCredentialsException;
import com.eventhub.exception.auth.RoleNotFoundException;
import com.eventhub.exception.auth.UserBlockedException;
import com.eventhub.exception.auth.UsernameAlreadyExistsException;
import com.eventhub.exception.category.CategoryAlreadyExistsException;
import com.eventhub.exception.category.CategoryInUseException;
import com.eventhub.exception.category.CategoryNotFoundException;
import com.eventhub.exception.event.EventAccessDeniedException;
import com.eventhub.exception.event.EventAlreadyCancelledException;
import com.eventhub.exception.event.EventAlreadyStartedException;
import com.eventhub.exception.event.EventCannotBeDeletedException;
import com.eventhub.exception.event.EventCannotBePublishedException;
import com.eventhub.exception.event.EventCannotBeUpdatedException;
import com.eventhub.exception.event.EventCapacityBelowTicketQuantityException;
import com.eventhub.exception.event.EventNotFinishedException;
import com.eventhub.exception.event.EventNotFoundException;
import com.eventhub.exception.event.EventNotPublishedException;
import com.eventhub.exception.event.InvalidEventDatesException;
import com.eventhub.exception.location.LocationInUseException;
import com.eventhub.exception.location.LocationNotFoundException;
import com.eventhub.exception.organizer.OrganizerApplicationAlreadyExistsException;
import com.eventhub.exception.organizer.OrganizerApplicationAlreadyReviewedException;
import com.eventhub.exception.organizer.OrganizerApplicationNotFoundException;
import com.eventhub.exception.organizer.UserAlreadyOrganizerException;
import com.eventhub.exception.payment.PaymentNotFoundException;
import com.eventhub.exception.registration.OrganizerOwnEventRegistrationException;
import com.eventhub.exception.registration.RegistrationAccessDeniedException;
import com.eventhub.exception.registration.RegistrationAlreadyExistsException;
import com.eventhub.exception.registration.RegistrationCannotBeCancelledException;
import com.eventhub.exception.registration.RegistrationNotFoundException;
import com.eventhub.exception.registration.TicketUnavailableException;
import com.eventhub.exception.review.ReviewAccessDeniedException;
import com.eventhub.exception.review.ReviewAlreadyExistsException;
import com.eventhub.exception.review.ReviewNotAllowedException;
import com.eventhub.exception.review.ReviewNotFoundException;
import com.eventhub.exception.ticket.TicketQuantityBelowSoldException;
import com.eventhub.exception.ticket.TicketQuantityExceededException;
import com.eventhub.exception.ticket.TicketTypeAlreadyExistsException;
import com.eventhub.exception.ticket.TicketTypeCannotBeManagedException;
import com.eventhub.exception.ticket.TicketTypeInUseException;
import com.eventhub.exception.ticket.TicketTypeNotFoundException;
import com.eventhub.exception.user.AdminSelfBlockException;
import com.eventhub.exception.user.UserAlreadyActiveException;
import com.eventhub.exception.user.UserAlreadyBlockedException;
import com.eventhub.exception.user.UserNotFoundException;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class ExceptionMessageTest {

    @Test
    void baseExceptionsShouldStoreMessages() {
        assertEquals("bad request", new BadRequestException("bad request").getMessage());
        assertEquals("conflict", new ConflictException("conflict").getMessage());
        assertEquals("forbidden", new ForbiddenActionException("forbidden").getMessage());
        assertEquals("not found", new ResourceNotFoundException("not found").getMessage());
    }

    @Test
    void authExceptionsShouldHaveExpectedMessages() {
        assertEquals("User with email test@mail.ru already exists", new EmailAlreadyExistsException("test@mail.ru").getMessage());
        assertEquals("Invalid email or password", new InvalidCredentialsException().getMessage());
        assertEquals("Role ROLE_USER not found", new RoleNotFoundException(RoleName.ROLE_USER).getMessage());
        assertEquals("User with email blocked@mail.ru is blocked", new UserBlockedException("blocked@mail.ru").getMessage());
        assertEquals("User with username polina already exists", new UsernameAlreadyExistsException("polina").getMessage());
    }

    @Test
    void categoryAndLocationExceptionsShouldHaveExpectedMessages() {
        assertEquals("Category with name IT already exists", new CategoryAlreadyExistsException("IT").getMessage());
        assertEquals("Category with id 1 can't be deleted because it is used by events", new CategoryInUseException(1L).getMessage());
        assertEquals("Category with id 1 not found", new CategoryNotFoundException(1L).getMessage());
        assertEquals("Location with id 1 cannot be deleted because it is used by events", new LocationInUseException(1L).getMessage());
        assertEquals("Location with id 1 not found", new LocationNotFoundException(1L).getMessage());
    }

    @Test
    void eventExceptionsShouldHaveExpectedMessages() {
        assertEquals("You do not have permission to manage events", new EventAccessDeniedException().getMessage());
        assertEquals("Event with id 1 is already cancelled", new EventAlreadyCancelledException(1L).getMessage());
        assertEquals("Event with id 1 has already started", new EventAlreadyStartedException(1L).getMessage());
        assertEquals("Event with id 1 can't be deleted. Published events should be cancelled instead",
                new EventCannotBeDeletedException(1L).getMessage());
        assertEquals("Event with id 1 can't be published", new EventCannotBePublishedException(1L).getMessage());
        assertEquals("Event with id 1 can't be updated", new EventCannotBeUpdatedException(1L).getMessage());
        assertEquals("Event with id 1 capacity can't be lower than existing ticket quantity",
                new EventCapacityBelowTicketQuantityException(1L).getMessage());
        assertEquals("Event with id 1 isn't finished yet", new EventNotFinishedException(1L).getMessage());
        assertEquals("Event with id 1 not found", new EventNotFoundException(1L).getMessage());
        assertEquals("Event with id 1 isn't published", new EventNotPublishedException(1L).getMessage());
        assertEquals("Event end date must be after start date", new InvalidEventDatesException().getMessage());
    }

    @Test
    void organizerExceptionsShouldHaveExpectedMessages() {
        assertEquals("User with id 1 already has an active organizer application",
                new OrganizerApplicationAlreadyExistsException(1L).getMessage());
        assertEquals("Organizer application with id 1 has already been reviewed",
                new OrganizerApplicationAlreadyReviewedException(1L).getMessage());
        assertEquals("Organizer application with id 1 not found", new OrganizerApplicationNotFoundException(1L).getMessage());
        assertEquals("User with id 1 is already an organizer", new UserAlreadyOrganizerException(1L).getMessage());
    }

    @Test
    void registrationAndPaymentExceptionsShouldHaveExpectedMessages() {
        assertEquals("Payment for registration with id 1 not found", new PaymentNotFoundException(1L).getMessage());
        assertEquals("Organizer can't buy tickets for own event with id 1", new OrganizerOwnEventRegistrationException(1L).getMessage());
        assertEquals("You don't have permission to manage registration with id 1", new RegistrationAccessDeniedException(1L).getMessage());
        assertEquals("User is already registered for event with id 1", new RegistrationAlreadyExistsException(1L).getMessage());
        assertEquals("Registration with id 1 can't be cancelled", new RegistrationCannotBeCancelledException(1L).getMessage());
        assertEquals("Registration with id 1 not found", new RegistrationNotFoundException(1L).getMessage());
        assertEquals("Ticket type with id 1 is unavailable", new TicketUnavailableException(1L).getMessage());
    }

    @Test
    void reviewExceptionsShouldHaveExpectedMessages() {
        assertEquals("You don't have permission to manage review with id 1", new ReviewAccessDeniedException(1L).getMessage());
        assertEquals("User has already reviewed event with id 1", new ReviewAlreadyExistsException(1L).getMessage());
        assertEquals("User can't review event with id 1 because there is no active registration",
                new ReviewNotAllowedException(1L).getMessage());
        assertEquals("Review with id 1 not found", new ReviewNotFoundException(1L).getMessage());
    }

    @Test
    void ticketExceptionsShouldHaveExpectedMessages() {
        assertEquals("Ticket type with id 1 can't have total quantity lower than sold quantity 5",
                new TicketQuantityBelowSoldException(1L, 5).getMessage());
        assertEquals("Total ticket quantity can't be greater than event capacity 100",
                new TicketQuantityExceededException(100).getMessage());
        assertEquals("Ticket type with name VIP already exists for this event",
                new TicketTypeAlreadyExistsException("VIP").getMessage());
        assertEquals("Ticket types can't be changed for event with id 1",
                new TicketTypeCannotBeManagedException(1L).getMessage());
        assertEquals("Ticket type with id 1 can't be deleted because it has active registrations",
                new TicketTypeInUseException(1L).getMessage());
        assertEquals("Ticket type with id 1 not found", new TicketTypeNotFoundException(1L).getMessage());
    }

    @Test
    void userExceptionsShouldHaveExpectedMessages() {
        assertEquals("Admin can't block own account", new AdminSelfBlockException().getMessage());
        assertEquals("User with id 1 is already active", new UserAlreadyActiveException(1L).getMessage());
        assertEquals("User with id 1 is already blocked", new UserAlreadyBlockedException(1L).getMessage());
        assertEquals("User with id 1 not found", new UserNotFoundException(1L).getMessage());
    }
}