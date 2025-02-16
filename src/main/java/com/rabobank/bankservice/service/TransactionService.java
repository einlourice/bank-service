package com.rabobank.bankservice.service;

import com.rabobank.bankservice.entity.Account;
import com.rabobank.bankservice.entity.Transaction;
import com.rabobank.bankservice.entity.TransactionType;
import com.rabobank.bankservice.repository.TransactionRepository;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Service
public class TransactionService {

    private final TransactionRepository transactionRepository;

    @Autowired
    TransactionService(TransactionRepository transactionRepository) {
        this.transactionRepository = transactionRepository;
    }

    @Transactional
    public void saveTransaction(Account account,
                                BigDecimal txnAmount,
                                BigDecimal calculatedFee,
                                TransactionType type) {
        Transaction transaction = new Transaction();
        transaction.setAccount(account);
        transaction.setAmount(txnAmount);
        transaction.setCalculatedFee(calculatedFee);
        transaction.setType(type);
        transaction.setTimestamp(LocalDateTime.now());

        transactionRepository.save(transaction);
    }
}
