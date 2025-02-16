package com.rabobank.bankservice.controller;

import com.rabobank.bankservice.entity.Account;
import com.rabobank.bankservice.model.AccountToUserAccountMapper;
import com.rabobank.bankservice.model.UserAccount;
import com.rabobank.bankservice.model.request.TransferTransaction;
import com.rabobank.bankservice.model.request.WithdrawTransaction;
import com.rabobank.bankservice.service.AccountService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/accounts")
public class AccountController {

    private final AccountService accountService;
    private final AccountToUserAccountMapper mapper;

    @Autowired
    AccountController(AccountService accountService,
                      AccountToUserAccountMapper accountToUserAccountMapper) {
        this.accountService = accountService;
        this.mapper = accountToUserAccountMapper;
    }

    @GetMapping("/")
    public ResponseEntity<UserAccount> getAccounts() {
        List<Account> accountList = accountService.getAccountList();
        return ResponseEntity.ok(mapper.accountsToUserAccount(accountList));
    }

    @PostMapping("/withdraw")
    public ResponseEntity<UserAccount> withdraw(@Valid @RequestBody WithdrawTransaction withdrawTransaction) {
        Account account = accountService.withdraw(withdrawTransaction);
        return ResponseEntity.ok(mapper.accountsToUserAccount(List.of(account)));
    }

    @PostMapping("/transfer")
    public ResponseEntity<UserAccount> transfer(@Valid @RequestBody TransferTransaction transferTransaction) {
        Account account = accountService.transfer(transferTransaction);
        return ResponseEntity.ok(mapper.accountsToUserAccount(List.of(account)));
    }
}
