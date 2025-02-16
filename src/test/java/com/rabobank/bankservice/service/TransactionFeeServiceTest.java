package com.rabobank.bankservice.service;

import com.rabobank.bankservice.entity.Account;
import com.rabobank.bankservice.entity.Card;
import com.rabobank.bankservice.entity.CardType;
import com.rabobank.bankservice.model.request.WithdrawTransaction;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.math.RoundingMode;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class TransactionFeeServiceTest {

    private static final BigDecimal CREDIT_CARD_FEE = new BigDecimal("0.01");

    @Mock
    private Account account;

    @Mock
    private Card card;

    private TransactionFeeService transactionFeeService;

    @BeforeEach
    void setUp() {
        when(account.getCard()).thenReturn(card);

        transactionFeeService = new TransactionFeeService(CREDIT_CARD_FEE);
    }

    @Test
    void shouldCalculateFeeForCreditCard() {
        WithdrawTransaction transaction = new WithdrawTransaction();
        transaction.setAmount(new BigDecimal("100.00"));
        when(card.getCardType()).thenReturn(CardType.CREDIT);

        BigDecimal fee = transactionFeeService.calculateFee(transaction, account).setScale(2, RoundingMode.HALF_UP);

        assertEquals(new BigDecimal("1.00"), fee);
    }

    @Test
    void shouldReturnZeroFeeForNonCreditCard() {
        WithdrawTransaction transaction = new WithdrawTransaction();
        transaction.setAmount(new BigDecimal("100.00"));
        when(card.getCardType()).thenReturn(CardType.DEBIT);

        BigDecimal fee = transactionFeeService.calculateFee(transaction, account);

        assertEquals(BigDecimal.ZERO, fee);
    }

    @Test
    void shouldHandleZeroAmount() {
        WithdrawTransaction transaction = new WithdrawTransaction();
        transaction.setAmount(BigDecimal.ZERO);
        when(card.getCardType()).thenReturn(CardType.CREDIT);

        BigDecimal fee = transactionFeeService.calculateFee(transaction, account);

        assertEquals(new BigDecimal("0.00"), fee);
    }
}
