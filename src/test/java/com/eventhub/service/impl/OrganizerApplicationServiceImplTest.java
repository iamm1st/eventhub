package com.eventhub.service.impl;

import com.eventhub.dto.request.OrganizerApplicationCreateRequest;
import com.eventhub.dto.request.OrganizerApplicationReviewRequest;
import com.eventhub.dto.response.OrganizerApplicationResponse;
import com.eventhub.entity.OrganizerApplication;
import com.eventhub.entity.Role;
import com.eventhub.entity.User;
import com.eventhub.enums.OrganizerApplicationStatus;
import com.eventhub.enums.RoleName;
import com.eventhub.exception.BadRequestException;
import com.eventhub.exception.organizer.OrganizerApplicationAlreadyExistsException;
import com.eventhub.mapper.OrganizerApplicationMapper;
import com.eventhub.repository.OrganizerApplicationRepository;
import com.eventhub.repository.RoleRepository;
import com.eventhub.repository.UserRepository;
import com.eventhub.security.CurrentUserProvider;
import com.eventhub.support.TestDataFactory;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyCollection;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class OrganizerApplicationServiceImplTest {

    @Mock
    private OrganizerApplicationRepository organizerApplicationRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private RoleRepository roleRepository;

    @Mock
    private CurrentUserProvider currentUserProvider;

    @Mock
    private OrganizerApplicationMapper organizerApplicationMapper;

    @InjectMocks
    private OrganizerApplicationServiceImpl organizerApplicationService;

    @Test
    void createApplicationShouldCreatePendingApplication() {
        User user = TestDataFactory.user(1L, RoleName.ROLE_USER);

        OrganizerApplicationCreateRequest request = OrganizerApplicationCreateRequest.builder()
                .organizationName(" Event Company ")
                .contactEmail("company@mail.com")
                .description(" We organize IT events ")
                .build();

        OrganizerApplicationResponse expectedResponse = OrganizerApplicationResponse.builder()
                .id(10L)
                .status(OrganizerApplicationStatus.PENDING)
                .build();

        when(currentUserProvider.getCurrentUserId()).thenReturn(user.getId());
        when(userRepository.findByIdWithRoles(user.getId())).thenReturn(Optional.of(user));
        when(organizerApplicationRepository.existsByUserIdAndStatusIn(any(), anyCollection())).thenReturn(false);
        when(organizerApplicationRepository.save(any(OrganizerApplication.class))).thenAnswer(invocation -> {
            OrganizerApplication application = invocation.getArgument(0);
            application.setId(10L);
            return application;
        });
        when(organizerApplicationMapper.toResponse(any(OrganizerApplication.class))).thenReturn(expectedResponse);

        OrganizerApplicationResponse actualResponse = organizerApplicationService.createApplication(request);

        assertEquals(expectedResponse, actualResponse);
        verify(organizerApplicationRepository).save(any(OrganizerApplication.class));
    }

    @Test
    void createApplicationShouldThrowExceptionWhenActiveApplicationExists() {
        User user = TestDataFactory.user(1L, RoleName.ROLE_USER);

        OrganizerApplicationCreateRequest request = OrganizerApplicationCreateRequest.builder()
                .organizationName("Event Company")
                .contactEmail("company@mail.com")
                .description("Description")
                .build();

        when(currentUserProvider.getCurrentUserId()).thenReturn(user.getId());
        when(userRepository.findByIdWithRoles(user.getId())).thenReturn(Optional.of(user));
        when(organizerApplicationRepository.existsByUserIdAndStatusIn(any(), anyCollection())).thenReturn(true);

        assertThrows(OrganizerApplicationAlreadyExistsException.class, () -> organizerApplicationService.createApplication(request));

        verify(organizerApplicationRepository, never()).save(any(OrganizerApplication.class));
    }

    @Test
    void approveApplicationShouldAddOrganizerRole() {
        User user = TestDataFactory.user(1L, RoleName.ROLE_USER);
        Role organizerRole = TestDataFactory.role(RoleName.ROLE_ORGANIZER);

        OrganizerApplication application = OrganizerApplication.builder()
                .id(10L)
                .user(user)
                .organizationName("Company")
                .contactEmail("company@mail.com")
                .description("Description")
                .status(OrganizerApplicationStatus.PENDING)
                .build();

        OrganizerApplicationReviewRequest request = OrganizerApplicationReviewRequest.builder()
                .adminComment("Approved")
                .build();

        OrganizerApplicationResponse expectedResponse = OrganizerApplicationResponse.builder()
                .id(application.getId())
                .status(OrganizerApplicationStatus.APPROVED)
                .build();

        when(organizerApplicationRepository.findById(application.getId())).thenReturn(Optional.of(application));
        when(userRepository.findByIdWithRoles(user.getId())).thenReturn(Optional.of(user));
        when(roleRepository.findByName(RoleName.ROLE_ORGANIZER)).thenReturn(Optional.of(organizerRole));
        when(organizerApplicationMapper.toResponse(application)).thenReturn(expectedResponse);

        OrganizerApplicationResponse actualResponse = organizerApplicationService.approveApplication(application.getId(), request);

        assertEquals(expectedResponse, actualResponse);
        assertEquals(OrganizerApplicationStatus.APPROVED, application.getStatus());
        assertTrue(user.getRoles().contains(organizerRole));
    }

    @Test
    void rejectApplicationShouldRequireAdminComment() {
        User user = TestDataFactory.user(1L, RoleName.ROLE_USER);

        OrganizerApplication application = OrganizerApplication.builder()
                .id(10L)
                .user(user)
                .organizationName("Company")
                .contactEmail("company@mail.com")
                .description("Description")
                .status(OrganizerApplicationStatus.PENDING)
                .build();

        OrganizerApplicationReviewRequest request = OrganizerApplicationReviewRequest.builder()
                .adminComment("")
                .build();

        when(organizerApplicationRepository.findById(application.getId())).thenReturn(Optional.of(application));

        assertThrows(BadRequestException.class, () -> organizerApplicationService.rejectApplication(application.getId(), request));
    }
}