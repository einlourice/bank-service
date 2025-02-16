package com.rabobank.bankservice.entity;

import com.rabobank.bankservice.error.InsufficientBalanceException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class AccountTest {

    private Account account;

    @BeforeEach
    void setUp() {
        account = new Account();
        account.setBalance(new BigDecimal("100.00"));
    }

    @Test
    void subtractBalance_SufficientBalance_Success() {
        BigDecimal amountToSubtract = new BigDecimal("50.00");
        BigDecimal expectedBalance = new BigDecimal("50.00");

        account.subtractBalance(amountToSubtract);

        assertEquals(expectedBalance, account.getBalance());
    }

    @Test
    void subtractBalance_WhenBalanceWouldBecomeNegative_ExceptionThrown() {
        BigDecimal amountToSubtract = new BigDecimal("150.00");

        InsufficientBalanceException exception = assertThrows(
                InsufficientBalanceException.class,
                () -> account.subtractBalance(amountToSubtract)
        );

        String expectedMessage = String.format(
                "Cannot subtract %s. Current balance: %s. Resulting balance would be negative.",
                amountToSubtract,
                account.getBalance()
        );

        assertEquals(expectedMessage, exception.getMessage());
        assertEquals(new BigDecimal("100.00"), account.getBalance());
    }

    @Test
    void subtractBalance_WhenExactlyZeroRemaining_Success() {
        BigDecimal amountToSubtract = new BigDecimal("100.00");

        account.subtractBalance(amountToSubtract);

        assertEquals(new BigDecimal("0.00"), account.getBalance());
    }

    @Test
    void addBalance_CorrectAddAmount_Success() {
        BigDecimal amountToAdd = new BigDecimal("50.00");
        BigDecimal expectedBalance = new BigDecimal("150.00");

        account.addBalance(amountToAdd);

        assertEquals(expectedBalance, account.getBalance());
    }

    @Test
    void addBalance_ShouldHandleZeroAmount() {
        BigDecimal originalBalance = account.getBalance();
        BigDecimal amountToAdd = BigDecimal.ZERO;

        account.addBalance(amountToAdd);

        assertEquals(originalBalance, account.getBalance());
    }

    @Test
    void subtractBalance_WithScale() {
        account.setBalance(new BigDecimal("100.50"));
        BigDecimal amountToSubtract = new BigDecimal("50.25");
        BigDecimal expectedBalance = new BigDecimal("50.25");

        account.subtractBalance(amountToSubtract);

        assertEquals(expectedBalance, account.getBalance());
    }

    @Test
    void addBalance_WithScale() {
        account.setBalance(new BigDecimal("100.50"));
        BigDecimal amountToAdd = new BigDecimal("50.25");
        BigDecimal expectedBalance = new BigDecimal("150.75");

        account.addBalance(amountToAdd);

        assertEquals(expectedBalance, account.getBalance());
    }
}
