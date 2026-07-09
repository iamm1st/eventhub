package com.eventhub.service.impl;

import com.eventhub.aspect.LogAction;
import com.eventhub.dto.request.OrganizerApplicationCreateRequest;
import com.eventhub.dto.request.OrganizerApplicationReviewRequest;
import com.eventhub.dto.response.OrganizerApplicationResponse;
import com.eventhub.entity.OrganizerApplication;
import com.eventhub.entity.Role;
import com.eventhub.entity.User;
import com.eventhub.enums.OrganizerApplicationStatus;
import com.eventhub.enums.RoleName;
import com.eventhub.enums.UserStatus;
import com.eventhub.exception.BadRequestException;
import com.eventhub.exception.ResourceNotFoundException;
import com.eventhub.exception.auth.RoleNotFoundException;
import com.eventhub.exception.auth.UserBlockedException;
import com.eventhub.exception.organizer.OrganizerApplicationAlreadyExistsException;
import com.eventhub.exception.organizer.OrganizerApplicationAlreadyReviewedException;
import com.eventhub.exception.organizer.OrganizerApplicationNotFoundException;
import com.eventhub.exception.organizer.UserAlreadyOrganizerException;
import com.eventhub.mapper.OrganizerApplicationMapper;
import com.eventhub.repository.OrganizerApplicationRepository;
import com.eventhub.repository.RoleRepository;
import com.eventhub.repository.UserRepository;
import com.eventhub.security.CurrentUserProvider;
import com.eventhub.service.OrganizerApplicationService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class OrganizerApplicationServiceImpl implements OrganizerApplicationService {

    private static final List<OrganizerApplicationStatus> ACTIVE_APPLICATION_STATUSES = List.of(
            OrganizerApplicationStatus.PENDING,
            OrganizerApplicationStatus.APPROVED);

    private final OrganizerApplicationRepository organizerApplicationRepository;
    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final CurrentUserProvider currentUserProvider;
    private final OrganizerApplicationMapper organizerApplicationMapper;

    @Override
    @Transactional
    public OrganizerApplicationResponse createApplication(OrganizerApplicationCreateRequest request) {
        Long currentUserId = currentUserProvider.getCurrentUserId();
        User user = findUserWithRoles(currentUserId);

        if (user.getStatus() == UserStatus.BLOCKED) {
            throw new UserBlockedException(user.getEmail());
        }

        if (hasRole(user)) {
            throw new UserAlreadyOrganizerException(user.getId());
        }

        if (organizerApplicationRepository.existsByUserIdAndStatusIn(user.getId(), ACTIVE_APPLICATION_STATUSES)) {
            throw new OrganizerApplicationAlreadyExistsException(user.getId());
        }

        OrganizerApplication application = OrganizerApplication.builder()
                .user(user)
                .organizationName(normalizeText(request.getOrganizationName()))
                .contactEmail(normalizeText(request.getContactEmail()))
                .contactPhone(normalizeNullableText(request.getContactPhone()))
                .description(normalizeText(request.getDescription()))
                .websiteUrl(normalizeNullableText(request.getWebsiteUrl()))
                .status(OrganizerApplicationStatus.PENDING)
                .build();

        OrganizerApplication savedApplication = organizerApplicationRepository.save(application);

        return organizerApplicationMapper.toResponse(savedApplication);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<OrganizerApplicationResponse> getMyApplications(Pageable pageable) {
        Long currentUserId = currentUserProvider.getCurrentUserId();

        return organizerApplicationRepository.findByUserId(currentUserId, pageable).map(organizerApplicationMapper::toResponse);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<OrganizerApplicationResponse> getApplications(OrganizerApplicationStatus status, Pageable pageable) {
        Page<OrganizerApplication> applications;

        if (status == null) {
            applications = organizerApplicationRepository.findAll(pageable);
        } else {
            applications = organizerApplicationRepository.findByStatus(status, pageable);
        }

        return applications.map(organizerApplicationMapper::toResponse);
    }

    @Override
    @LogAction(action = "APPROVE_ORGANIZER_APPLICATION", entityType = "ORGANIZER_APPLICATION", entityIdArgIndex = 0)
    @Transactional
    public OrganizerApplicationResponse approveApplication(Long id, OrganizerApplicationReviewRequest request) {
        OrganizerApplication application = findApplicationById(id);
        checkApplicationIsPending(application);

        User applicant = findUserWithRoles(application.getUser().getId());
        Role organizerRole = roleRepository.findByName(RoleName.ROLE_ORGANIZER)
                .orElseThrow(() -> new RoleNotFoundException(RoleName.ROLE_ORGANIZER));

        applicant.getRoles().add(organizerRole);

        application.setUser(applicant);
        application.setStatus(OrganizerApplicationStatus.APPROVED);
        application.setAdminComment(getAdminComment(request));
        application.setReviewedAt(LocalDateTime.now());

        return organizerApplicationMapper.toResponse(application);
    }

    @Override
    @LogAction(action = "REJECT_ORGANIZER_APPLICATION", entityType = "ORGANIZER_APPLICATION", entityIdArgIndex = 0)
    @Transactional
    public OrganizerApplicationResponse rejectApplication(Long id, OrganizerApplicationReviewRequest request) {
        OrganizerApplication application = findApplicationById(id);
        checkApplicationIsPending(application);

        String adminComment = getAdminComment(request);

        if (adminComment == null || adminComment.isBlank()) {
            throw new BadRequestException("Admin comment is required to reject organizer application");
        }

        application.setStatus(OrganizerApplicationStatus.REJECTED);
        application.setAdminComment(adminComment);
        application.setReviewedAt(LocalDateTime.now());

        return organizerApplicationMapper.toResponse(application);
    }

    private OrganizerApplication findApplicationById(Long id) {
        return organizerApplicationRepository.findById(id).orElseThrow(() -> new OrganizerApplicationNotFoundException(id));
    }

    private User findUserWithRoles(Long userId) {
        return userRepository.findByIdWithRoles(userId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "User with id " + userId + " not found"));
    }

    private void checkApplicationIsPending(OrganizerApplication application) {
        if (application.getStatus() != OrganizerApplicationStatus.PENDING) {
            throw new OrganizerApplicationAlreadyReviewedException(application.getId());
        }
    }

    private boolean hasRole(User user) {
        return user.getRoles().stream().anyMatch(role -> role.getName() == RoleName.ROLE_ORGANIZER);
    }

    private String getAdminComment(OrganizerApplicationReviewRequest request) {
        if (request == null) {
            return null;
        }

        return normalizeNullableText(request.getAdminComment());
    }

    private String normalizeText(String value) {
        return value.trim();
    }

    private String normalizeNullableText(String value) {
        if (value == null || value.isBlank()) {
            return null;
        }

        return value.trim();
    }
}