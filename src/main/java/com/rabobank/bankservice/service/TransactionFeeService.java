package com.rabobank.bankservice.service;

import com.rabobank.bankservice.entity.Account;
import com.rabobank.bankservice.entity.CardType;
import com.rabobank.bankservice.model.request.BalanceTransaction;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;

@Service
public class TransactionFeeService {

    private final BigDecimal creditCardFee;

    TransactionFeeService(@Value("${credit.card.fee:0.01}") BigDecimal creditCardFee) {
        this.creditCardFee = creditCardFee;
    }

    public BigDecimal calculateFee(BalanceTransaction balanceTransaction, Account account) {
        if (CardType.CREDIT.equals(account.getCard().getCardType())) {
            return balanceTransaction.getAmount().multiply(creditCardFee);
        }

        return BigDecimal.ZERO;
    }
}
