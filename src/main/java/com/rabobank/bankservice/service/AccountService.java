package com.rabobank.bankservice.service;

import com.rabobank.bankservice.context.UserContext;
import com.rabobank.bankservice.entity.Account;
import com.rabobank.bankservice.entity.TransactionType;
import com.rabobank.bankservice.error.AccountNotFoundException;
import com.rabobank.bankservice.model.request.TransferTransaction;
import com.rabobank.bankservice.model.request.WithdrawTransaction;
import com.rabobank.bankservice.repository.AccountRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;

@Service
public class AccountService {

    private final AccountRepository accountRepository;
    private final TransactionService transactionService;
    private final TransactionFeeService transactionFeeService;
    private final UserContext userContext;

    @Autowired
    AccountService(AccountRepository accountRepository,
                   TransactionService transactionService,
                   TransactionFeeService transactionFeeService,
                   UserContext userContext) {
        this.accountRepository = accountRepository;
        this.transactionService = transactionService;
        this.transactionFeeService = transactionFeeService;
        this.userContext = userContext;
    }

    @Transactional(readOnly = true)
    public List<Account> getAccountList() {
        Long userId = userContext.getCurrentUser().getId();
        return accountRepository.findAllByUserId(userId)
                .filter(list -> !list.isEmpty())
                .orElseThrow(() -> new AccountNotFoundException("Account not found with user id: " + userId));
    }

    @Transactional
    public Account withdraw(WithdrawTransaction withdrawTransaction) {
        Account account = accountRepository.findById(withdrawTransaction.getAccount())
                .orElseThrow(() -> new AccountNotFoundException("Account not found"));

        //check if currentUser is the owner of the account
        userContext.isAuthorized(account);

        BigDecimal totalFee = transactionFeeService.calculateFee(withdrawTransaction, account);
        BigDecimal totalAmount = withdrawTransaction.getAmount().add(totalFee);
        account.subtractBalance(totalAmount);

        accountRepository.save(account);
        transactionService.saveTransaction(account, withdrawTransaction.getAmount(), totalFee, TransactionType.WITHDRAWAL);

        return account;
    }

    @Transactional
    public Account transfer(TransferTransaction transferTransaction) {
        Account sourceAccount = accountRepository.findById(transferTransaction.getSourceAccountId())
                .orElseThrow(() -> new AccountNotFoundException("Source account not found"));

        //check if currentUser is the owner of the sourceAccount
        userContext.isAuthorized(sourceAccount);

        Account targetAccount = accountRepository.findById(transferTransaction.getTargetAccountId())
                .orElseThrow(() -> new AccountNotFoundException("Target account not found"));

        BigDecimal totalFee = transactionFeeService.calculateFee(transferTransaction, sourceAccount);
        BigDecimal totalAmount = transferTransaction.getAmount().add(totalFee);

        sourceAccount.subtractBalance(totalAmount);
        targetAccount.addBalance(transferTransaction.getAmount());

        accountRepository.save(sourceAccount);
        accountRepository.save(targetAccount);

        transactionService.saveTransaction(sourceAccount, transferTransaction.getAmount(), totalFee, TransactionType.TRANSFER);
        transactionService.saveTransaction(targetAccount, transferTransaction.getAmount(), totalFee, TransactionType.TRANSFER);

        return sourceAccount;
    }

}
