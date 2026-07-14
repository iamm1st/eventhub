package com.eventhub.security;

import com.eventhub.entity.User;
import com.eventhub.enums.RoleName;
import com.eventhub.support.TestDataFactory;
import io.jsonwebtoken.MalformedJwtException;
import jakarta.servlet.FilterChain;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.context.SecurityContextHolder;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class JwtAuthenticationFilterTest {

    @Mock
    private JwtService jwtService;

    @Mock
    private CustomUserDetailsService customUserDetailsService;

    @Mock
    private RestAuthenticationEntryPoint restAuthenticationEntryPoint;

    @Mock
    private RestAccessDeniedHandler restAccessDeniedHandler;

    @Mock
    private FilterChain filterChain;

    @InjectMocks
    private JwtAuthenticationFilter jwtAuthenticationFilter;

    @BeforeEach
    void clearContextBeforeTest() {
        SecurityContextHolder.clearContext();
    }

    @AfterEach
    void clearContextAfterTest() {
        SecurityContextHolder.clearContext();
    }

    @Test
    void doFilterInternalShouldContinueChainWhenAuthorizationHeaderIsMissing() throws Exception {
        MockHttpServletRequest request = new MockHttpServletRequest();
        MockHttpServletResponse response = new MockHttpServletResponse();

        jwtAuthenticationFilter.doFilterInternal(request, response, filterChain);

        verify(filterChain).doFilter(request, response);
        verifyNoInteractions(
                jwtService,
                customUserDetailsService,
                restAuthenticationEntryPoint,
                restAccessDeniedHandler);
        assertNull(SecurityContextHolder.getContext().getAuthentication());
    }

    @Test
    void doFilterInternalShouldAuthenticateUserWhenTokenIsValid() throws Exception {
        User user = TestDataFactory.user(1L, RoleName.ROLE_USER);
        CustomUserDetails userDetails = new CustomUserDetails(user);

        MockHttpServletRequest request = new MockHttpServletRequest();
        request.addHeader("Authorization", "Bearer valid-token");

        MockHttpServletResponse response = new MockHttpServletResponse();

        when(jwtService.extractUsername("valid-token")).thenReturn(user.getEmail());
        when(customUserDetailsService.loadUserByUsername(user.getEmail())).thenReturn(userDetails);
        when(jwtService.isTokenValid("valid-token", userDetails)).thenReturn(true);

        jwtAuthenticationFilter.doFilterInternal(request, response, filterChain);

        assertNotNull(SecurityContextHolder.getContext().getAuthentication());
        assertSame(userDetails, SecurityContextHolder.getContext().getAuthentication().getPrincipal());
        assertEquals(user.getEmail(), SecurityContextHolder.getContext().getAuthentication().getName());

        verify(filterChain).doFilter(request, response);
    }

    @Test
    void doFilterInternalShouldReturnForbiddenWhenUserIsBlocked() throws Exception {
        User blockedUser = TestDataFactory.blockedUser(1L, RoleName.ROLE_USER);
        CustomUserDetails userDetails = new CustomUserDetails(blockedUser);

        MockHttpServletRequest request = new MockHttpServletRequest();
        request.addHeader("Authorization", "Bearer valid-token");

        MockHttpServletResponse response = new MockHttpServletResponse();

        when(jwtService.extractUsername("valid-token")).thenReturn(blockedUser.getEmail());
        when(customUserDetailsService.loadUserByUsername(blockedUser.getEmail())).thenReturn(userDetails);

        jwtAuthenticationFilter.doFilterInternal(request, response, filterChain);

        verify(restAccessDeniedHandler).handle(eq(request), eq(response), any(AccessDeniedException.class));
        verify(filterChain, never()).doFilter(any(), any());
        assertNull(SecurityContextHolder.getContext().getAuthentication());
    }

    @Test
    void doFilterInternalShouldReturnUnauthorizedWhenTokenIsInvalid() throws Exception {
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.addHeader("Authorization", "Bearer invalid-token");

        MockHttpServletResponse response = new MockHttpServletResponse();

        when(jwtService.extractUsername("invalid-token")).thenThrow(new MalformedJwtException("Invalid JWT token"));

        jwtAuthenticationFilter.doFilterInternal(request, response, filterChain);

        verify(restAuthenticationEntryPoint).commence(eq(request), eq(response), any(BadCredentialsException.class));
        verify(filterChain, never()).doFilter(any(), any());
        assertNull(SecurityContextHolder.getContext().getAuthentication());
    }
}