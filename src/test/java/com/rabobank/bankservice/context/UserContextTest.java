package com.rabobank.bankservice.context;

import com.rabobank.bankservice.entity.Account;
import com.rabobank.bankservice.entity.User;
import com.rabobank.bankservice.error.UnauthorizedException;
import jakarta.servlet.http.HttpServletRequest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class UserContextTest {

    @Mock
    private HttpServletRequest request;

    private UserContext userContext;

    @BeforeEach
    void setUp() {
        userContext = new UserContext(request);
    }

    @Test
    void getCurrentUser_WhenUserExists_ReturnsUser() {
        User expectedUser = new User();
        expectedUser.setId(1L);
        when(request.getAttribute("currentUser")).thenReturn(expectedUser);

        User actualUser = userContext.getCurrentUser();

        assertNotNull(actualUser);
        assertEquals(expectedUser.getId(), actualUser.getId());
    }

    @Test
    void getCurrentUser_WhenUserDoesNotExist_ThrowsUnauthorizedException() {
        when(request.getAttribute("currentUser")).thenReturn(null);

        UnauthorizedException exception = assertThrows(
                UnauthorizedException.class,
                () -> userContext.getCurrentUser()
        );
        assertEquals("User not found in context", exception.getMessage());
    }

    @Test
    void isAuthorized_WhenUserMatchesAccount_DoesNotThrowException() {
        User user = new User();
        user.setId(1L);
        when(request.getAttribute("currentUser")).thenReturn(user);

        User accountUser = new User();
        accountUser.setId(1L);
        Account account = new Account();
        account.setUser(accountUser);

        assertDoesNotThrow(() -> userContext.isAuthorized(account));
    }

    @Test
    void isAuthorized_WhenUserDoesNotMatchAccount_ThrowsUnauthorizedException() {
        User user = new User();
        user.setId(1L);
        when(request.getAttribute("currentUser")).thenReturn(user);

        User accountUser = new User();
        accountUser.setId(2L);
        Account account = new Account();
        account.setUser(accountUser);

        UnauthorizedException exception = assertThrows(
                UnauthorizedException.class,
                () -> userContext.isAuthorized(account)
        );
        assertEquals("User id and account ID mismatch", exception.getMessage());
    }
}
