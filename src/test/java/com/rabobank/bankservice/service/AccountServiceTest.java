package com.rabobank.bankservice.service;

import com.rabobank.bankservice.context.UserContext;
import com.rabobank.bankservice.entity.*;
import com.rabobank.bankservice.error.AccountNotFoundException;
import com.rabobank.bankservice.model.request.TransferTransaction;
import com.rabobank.bankservice.model.request.WithdrawTransaction;
import com.rabobank.bankservice.repository.AccountRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AccountServiceTest {

    private static final BigDecimal INITIAL_BALANCE = new BigDecimal("1000.00");
    private static final BigDecimal TRANSACTION_AMOUNT = new BigDecimal("100.00");
    private static final BigDecimal TRANSACTION_FEE = new BigDecimal("1.00");

    @Mock
    private AccountRepository accountRepository;

    @Mock
    private TransactionService transactionService;

    @Mock
    private TransactionFeeService transactionFeeService;

    @Mock
    private UserContext userContext;

    @Mock
    private User user;

    @InjectMocks
    private AccountService accountService;

    private Account sourceAccount;
    private Account targetAccount;

    @BeforeEach
    void setUp() {
        Card debitCard = new Card();
        debitCard.setCardType(CardType.DEBIT);
        sourceAccount = new Account();
        sourceAccount.setId(1L);
        sourceAccount.setBalance(INITIAL_BALANCE);
        sourceAccount.setCard(debitCard);

        targetAccount = new Account();
        targetAccount.setId(2L);
        targetAccount.setBalance(INITIAL_BALANCE);
    }

    @Test
    void getAccountList_ShouldReturnAccounts_WhenAccountsExist() {
        Long userId = 1L;
        List<Account> expectedAccounts = Arrays.asList(sourceAccount, targetAccount);
        when(userContext.getCurrentUser()).thenReturn(user);
        when(user.getId()).thenReturn(userId);
        when(accountRepository.findAllByUserId(userId)).thenReturn(Optional.of(expectedAccounts));


        List<Account> accounts = accountService.getAccountList();

        assertEquals(expectedAccounts, accounts);
        verify(accountRepository).findAllByUserId(userId);
    }

    @Test
    void getAccountList_ShouldThrowException_WhenNoAccountsFound() {
        Long userId = 1L;
        when(userContext.getCurrentUser()).thenReturn(user);
        when(user.getId()).thenReturn(userId);
        when(accountRepository.findAllByUserId(userId)).thenReturn(Optional.of(List.of()));

        assertThrows(AccountNotFoundException.class,
                () -> accountService.getAccountList());
    }

    @Test
    void withdraw_WhenSufficientBalance_Success() {
        WithdrawTransaction withdrawTransaction = new WithdrawTransaction();
        withdrawTransaction.setAccount(1L);
        withdrawTransaction.setAmount(TRANSACTION_AMOUNT);

        when(accountRepository.findById(1L)).thenReturn(Optional.of(sourceAccount));
        when(transactionFeeService.calculateFee(withdrawTransaction, sourceAccount)).thenReturn(TRANSACTION_FEE);
        when(accountRepository.save(any(Account.class))).thenReturn(sourceAccount);

        Account updatedAccount = accountService.withdraw(withdrawTransaction);

        BigDecimal expectedBalance = INITIAL_BALANCE
                .subtract(TRANSACTION_AMOUNT)
                .subtract(TRANSACTION_FEE);
        assertEquals(expectedBalance, updatedAccount.getBalance());

        verify(accountRepository).save(sourceAccount);
        verify(transactionService).saveTransaction(
                eq(sourceAccount),
                eq(TRANSACTION_AMOUNT),
                eq(TRANSACTION_FEE),
                eq(TransactionType.WITHDRAWAL)
        );
    }

    @Test
    void transfer_WhenSufficientBalance_Success() {
        TransferTransaction transferTransaction = new TransferTransaction();
        transferTransaction.setSourceAccountId(1L);
        transferTransaction.setTargetAccountId(2L);
        transferTransaction.setAmount(TRANSACTION_AMOUNT);

        when(accountRepository.findById(1L)).thenReturn(Optional.of(sourceAccount));
        when(accountRepository.findById(2L)).thenReturn(Optional.of(targetAccount));
        when(transactionFeeService.calculateFee(transferTransaction, sourceAccount)).thenReturn(TRANSACTION_FEE);
        when(accountRepository.save(any(Account.class))).thenReturn(sourceAccount);

        Account updatedAccount = accountService.transfer(transferTransaction);

        BigDecimal expectedSourceBalance = INITIAL_BALANCE
                .subtract(TRANSACTION_AMOUNT)
                .subtract(TRANSACTION_FEE);
        BigDecimal expectedTargetBalance = INITIAL_BALANCE
                .add(TRANSACTION_AMOUNT);

        assertEquals(expectedSourceBalance, sourceAccount.getBalance());
        assertEquals(expectedTargetBalance, targetAccount.getBalance());

        verify(accountRepository).save(sourceAccount);
        verify(accountRepository).save(targetAccount);
        verify(transactionService, times(2)).saveTransaction(
                any(Account.class),
                eq(TRANSACTION_AMOUNT),
                eq(TRANSACTION_FEE),
                eq(TransactionType.TRANSFER)
        );
    }

    @Test
    void transfer_WhenSourceAccountNotFound_ExceptionThrown() {
        TransferTransaction transferTransaction = new TransferTransaction();
        transferTransaction.setSourceAccountId(1L);
        transferTransaction.setTargetAccountId(2L);
        when(accountRepository.findById(1L)).thenReturn(Optional.empty());

        assertThrows(AccountNotFoundException.class,
                () -> accountService.transfer(transferTransaction));

        verify(accountRepository, never()).save(any());
        verify(transactionService, never()).saveTransaction(
                any(), any(), any(), any()
        );
    }

    @Test
    void withdraw_WhenAccountNotFound_ExceptionThrown() {
        WithdrawTransaction withdrawTransaction = new WithdrawTransaction();
        withdrawTransaction.setAccount(1L);
        when(accountRepository.findById(1L)).thenReturn(Optional.empty());

        assertThrows(AccountNotFoundException.class,
                () -> accountService.withdraw(withdrawTransaction));

        verify(accountRepository, never()).save(any());
        verify(transactionService, never()).saveTransaction(
                any(), any(), any(), any()
        );
    }
}
