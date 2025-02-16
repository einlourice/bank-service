package com.rabobank.bankservice.service;

import com.rabobank.bankservice.entity.User;
import com.rabobank.bankservice.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CustomUserAuthServiceTest {

    private final String TEST_EMAIL = "test@example.com";
    private final String TEST_PASSWORD = "hashedPassword123";
    @Mock
    private UserRepository userRepository;
    @InjectMocks
    private CustomUserAuthService userAuthService;
    private User testUser;

    @BeforeEach
    void setUp() {
        testUser = new User();
        testUser.setEmail(TEST_EMAIL);
        testUser.setPassword(TEST_PASSWORD);
    }

    @Test
    void loadUserByUsername_WhenUserExists_ReturnsUserDetails() {
        when(userRepository.findByEmail(TEST_EMAIL)).thenReturn(Optional.of(testUser));

        UserDetails userDetails = userAuthService.loadUserByUsername(TEST_EMAIL);

        assertNotNull(userDetails);
        assertEquals(TEST_EMAIL, userDetails.getUsername());
        assertEquals(TEST_PASSWORD, userDetails.getPassword());
        assertTrue(userDetails.getAuthorities().contains(new SimpleGrantedAuthority("ROLE_USER")));
        assertEquals(1, userDetails.getAuthorities().size());
        assertTrue(userDetails.isEnabled());
        assertTrue(userDetails.isAccountNonExpired());
        assertTrue(userDetails.isCredentialsNonExpired());
        assertTrue(userDetails.isAccountNonLocked());
    }

    @Test
    void loadUserByUsername_WhenUserDoesNotExist_ThrowsUsernameNotFoundException() {
        String nonExistentEmail = "nonexistent@example.com";
        when(userRepository.findByEmail(nonExistentEmail)).thenReturn(Optional.empty());

        UsernameNotFoundException exception = assertThrows(
                UsernameNotFoundException.class,
                () -> userAuthService.loadUserByUsername(nonExistentEmail)
        );
        assertEquals("User not found: " + nonExistentEmail, exception.getMessage());
    }

    @Test
    void loadUserByUsername_WhenEmailIsNull_ThrowsUsernameNotFoundException() {
        assertThrows(
                UsernameNotFoundException.class,
                () -> userAuthService.loadUserByUsername(null)
        );
    }

    @Test
    void loadUserByUsername_WhenRepositoryThrowsException_PropagatesException() {
        when(userRepository.findByEmail(TEST_EMAIL)).thenThrow(new RuntimeException("Database error"));

        assertThrows(
                RuntimeException.class,
                () -> userAuthService.loadUserByUsername(TEST_EMAIL)
        );
    }
}