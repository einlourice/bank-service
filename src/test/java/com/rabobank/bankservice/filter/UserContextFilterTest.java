package com.rabobank.bankservice.filter;

import com.rabobank.bankservice.entity.User;
import com.rabobank.bankservice.repository.UserRepository;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;

import java.io.IOException;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UserContextFilterTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private HttpServletRequest request;

    @Mock
    private HttpServletResponse response;

    @Mock
    private FilterChain filterChain;

    @Mock
    private Authentication authentication;

    @Mock
    private SecurityContext securityContext;

    @InjectMocks
    private UserContextFilter userContextFilter;

    private User testUser;

    @BeforeEach
    void setUp() {
        testUser = new User();
        testUser.setId(1L);
        testUser.setEmail("test@example.com");

        SecurityContextHolder.setContext(securityContext);
    }

    @Test
    void doFilterInternal_WhenAuthenticatedAndUserFound_SetsUserAttribute() throws ServletException, IOException {
        when(securityContext.getAuthentication()).thenReturn(authentication);
        when(authentication.isAuthenticated()).thenReturn(true);
        when(authentication.getName()).thenReturn("test@example.com");
        when(userRepository.findByEmail("test@example.com")).thenReturn(Optional.of(testUser));

        userContextFilter.doFilterInternal(request, response, filterChain);

        verify(request).setAttribute("currentUser", testUser);
        verify(filterChain).doFilter(request, response);
    }

    @Test
    void doFilterInternal_WhenAuthenticatedButUserNotFound_ThrowsRuntimeException() throws ServletException, IOException {
        when(securityContext.getAuthentication()).thenReturn(authentication);
        when(authentication.isAuthenticated()).thenReturn(true);
        when(authentication.getName()).thenReturn("test@example.com");
        when(userRepository.findByEmail("test@example.com")).thenReturn(Optional.empty());

        RuntimeException exception = assertThrows(
                RuntimeException.class,
                () -> userContextFilter.doFilterInternal(request, response, filterChain)
        );
        assertEquals("User not found", exception.getMessage());
        verify(filterChain, never()).doFilter(request, response);
    }

    @Test
    void doFilterInternal_WhenNotAuthenticated_ContinuesFilterChain() throws ServletException, IOException {
        when(securityContext.getAuthentication()).thenReturn(authentication);
        when(authentication.isAuthenticated()).thenReturn(false);

        userContextFilter.doFilterInternal(request, response, filterChain);

        verify(request, never()).setAttribute(eq("currentUser"), any());
        verify(filterChain).doFilter(request, response);
    }

    @Test
    void doFilterInternal_WhenNoAuthentication_ContinuesFilterChain() throws ServletException, IOException {
        when(securityContext.getAuthentication()).thenReturn(null);

        userContextFilter.doFilterInternal(request, response, filterChain);

        verify(request, never()).setAttribute(eq("currentUser"), any());
        verify(filterChain).doFilter(request, response);
    }

    @Test
    void doFilterInternal_WhenRepositoryThrowsException_PropagatesException() throws ServletException, IOException {
        when(securityContext.getAuthentication()).thenReturn(authentication);
        when(authentication.isAuthenticated()).thenReturn(true);
        when(authentication.getName()).thenReturn("test@example.com");
        when(userRepository.findByEmail(anyString())).thenThrow(new RuntimeException("Database error"));

        RuntimeException exception = assertThrows(
                RuntimeException.class,
                () -> userContextFilter.doFilterInternal(request, response, filterChain)
        );
        assertEquals("Database error", exception.getMessage());
        verify(filterChain, never()).doFilter(request, response);
    }
}
