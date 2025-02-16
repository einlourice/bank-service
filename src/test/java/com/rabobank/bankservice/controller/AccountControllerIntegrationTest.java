package com.rabobank.bankservice.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.rabobank.bankservice.entity.*;
import com.rabobank.bankservice.model.request.TransferTransaction;
import com.rabobank.bankservice.model.request.WithdrawTransaction;
import com.rabobank.bankservice.repository.AccountRepository;
import com.rabobank.bankservice.repository.TransactionRepository;
import com.rabobank.bankservice.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.util.List;

import static org.hamcrest.Matchers.hasSize;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.httpBasic;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
class AccountControllerIntegrationTest {

    private static final String TEST_PASSWORD = "123";

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private AccountRepository accountRepository;

    @Autowired
    private TransactionRepository transactionRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    private User testUser;
    private User secondTestUser;
    private Account debitAccount1;
    private Account creditAccount1;
    private Account creditAccount2;


    @BeforeEach
    void setUp() {
        transactionRepository.deleteAll();
        accountRepository.deleteAll();
        userRepository.deleteAll();

        testUser = new User();
        testUser.setName("Test-User1");
        testUser.setEmail("test@example.com");
        testUser.setPassword(passwordEncoder.encode(TEST_PASSWORD));
        userRepository.save(testUser);

        secondTestUser = new User();
        secondTestUser.setName("Test-User2");
        secondTestUser.setEmail("test2@example.com");
        secondTestUser.setPassword(passwordEncoder.encode(TEST_PASSWORD));
        userRepository.save(secondTestUser);

        Card creditCard = new Card();
        creditCard.setCardType(CardType.CREDIT);
        creditCard.setCardNumber("creditCard#1");

        Card debitCard = new Card();
        debitCard.setCardType(CardType.DEBIT);
        debitCard.setCardNumber("debitCard#1");

        Card creditCard2 = new Card();
        creditCard.setCardType(CardType.CREDIT);
        creditCard.setCardNumber("creditCard#2");

        debitAccount1 = new Account();
        debitAccount1.setUser(testUser);
        debitAccount1.setBalance(new BigDecimal("1000.00"));
        debitAccount1.setCard(debitCard);
        accountRepository.save(debitAccount1);

        creditAccount1 = new Account();
        creditAccount1.setUser(testUser);
        creditAccount1.setBalance(new BigDecimal("2000.00"));
        creditAccount1.setCard(creditCard);
        accountRepository.save(creditAccount1);

        creditAccount2 = new Account();
        creditAccount2.setUser(secondTestUser);
        creditAccount2.setBalance(new BigDecimal("1000.00"));
        creditAccount2.setCard(creditCard2);
        accountRepository.save(creditAccount2);
    }

    /**
     * Requirements for #2, #3 and #6
     */
    @Test
    void getAccounts_ShouldReturnUserAccounts() throws Exception {
        mockMvc.perform(get("/api/accounts/")
                        .with(httpBasic(testUser.getEmail(), TEST_PASSWORD)))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.userName").value("Test-User1"))
                .andExpect(jsonPath("$.userEmail").value("test@example.com"))
                .andExpect(jsonPath("$.accountDetails", hasSize(2)))
                .andExpect(jsonPath("$.accountDetails[0].cardNumber").value("debitCard#1"))
                .andExpect(jsonPath("$.accountDetails[0].cardType").value("DEBIT"))
                .andExpect(jsonPath("$.accountDetails[0].currentBalance").value(1000.0))
                .andExpect(jsonPath("$.accountDetails[1].cardNumber").value("creditCard#2"))
                .andExpect(jsonPath("$.accountDetails[1].cardType").value("CREDIT"))
                .andExpect(jsonPath("$.accountDetails[1].currentBalance").value(2000.0));
    }

    /**
     * Requirements for #4 and #7
     */
    @Test
    void withdraw_WhenSufficientBalance_Success() throws Exception {
        WithdrawTransaction withdrawTransaction = new WithdrawTransaction();
        withdrawTransaction.setAccount(debitAccount1.getId());
        withdrawTransaction.setAmount(new BigDecimal("100.00"));

        mockMvc.perform(post("/api/accounts/withdraw")
                        .with(httpBasic(testUser.getEmail(), TEST_PASSWORD))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(withdrawTransaction)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.userName").value("Test-User1"))
                .andExpect(jsonPath("$.userEmail").value("test@example.com"))
                .andExpect(jsonPath("$.accountDetails", hasSize(1)))
                .andExpect(jsonPath("$.accountDetails[0].cardType").value("DEBIT"))
                .andExpect(jsonPath("$.accountDetails[0].currentBalance").value("900.00"));

        //Check audit
        List<Transaction> transactionList = transactionRepository.findAllByAccountId(debitAccount1.getId()).orElseThrow();
        assertEquals(1, transactionList.size());
        assertEquals(withdrawTransaction.getAccount(), transactionList.get(0).getAccount().getId());
        assertEquals(withdrawTransaction.getAmount(), transactionList.get(0).getAmount());

        Account updatedAccount = accountRepository.findById(debitAccount1.getId()).orElseThrow();
        assertEquals(new BigDecimal("900.00"), updatedAccount.getBalance());
    }

    @Test
    void withdraw_UserIdAndAccountIdMismatch_Unauthorized() throws Exception {
        WithdrawTransaction withdrawTransaction = new WithdrawTransaction();
        withdrawTransaction.setAccount(debitAccount1.getId());
        withdrawTransaction.setAmount(new BigDecimal("100.00"));

        mockMvc.perform(post("/api/accounts/withdraw")
                        .with(httpBasic(secondTestUser.getEmail(), TEST_PASSWORD))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(withdrawTransaction)))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.message").value(
                        "User id and account ID mismatch"));
    }

    /**
     * Requirements for #1
     */
    @Test
    void withdraw_WhenInsufficientBalance_Fail() throws Exception {
        WithdrawTransaction withdrawTransaction = new WithdrawTransaction();
        withdrawTransaction.setAccount(debitAccount1.getId());
        withdrawTransaction.setAmount(new BigDecimal("2000.00"));

        mockMvc.perform(post("/api/accounts/withdraw")
                        .with(httpBasic(testUser.getEmail(), TEST_PASSWORD))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(withdrawTransaction)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value(
                        "Cannot subtract 2000.00. Current balance: 1000.00. Resulting balance would be negative."));
    }

    /**
     * Requirement #7
     * Credit transaction should have a fee
     */
    @Test
    void creditCardFee_WhenUsingCreditCard_Success() throws Exception {
        WithdrawTransaction withdrawTransaction = new WithdrawTransaction();
        withdrawTransaction.setAccount(creditAccount1.getId());
        withdrawTransaction.setAmount(new BigDecimal("100.00"));

        mockMvc.perform(post("/api/accounts/withdraw")
                        .with(httpBasic(testUser.getEmail(), TEST_PASSWORD))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(withdrawTransaction)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.accountDetails[0].currentBalance").value("1899.00")); // 2000 - 100 - 1 (fee)

        //Check audit
        List<Transaction> transactionList = transactionRepository.findAllByAccountId(creditAccount1.getId()).orElseThrow();
        assertEquals(1, transactionList.size());
        assertEquals(withdrawTransaction.getAccount(), transactionList.get(0).getAccount().getId());
        assertEquals(withdrawTransaction.getAmount(), transactionList.get(0).getAmount());

        Account updatedAccount = accountRepository.findById(creditAccount1.getId()).orElseThrow();
        assertEquals(new BigDecimal("1899.00"), updatedAccount.getBalance());
    }

    /**
     * Requirement #5
     */
    @Test
    void transfer_CreditTransfer_Success() throws Exception {
        TransferTransaction transferTransaction = new TransferTransaction();
        transferTransaction.setSourceAccountId(creditAccount1.getId());
        transferTransaction.setTargetAccountId(creditAccount2.getId());
        transferTransaction.setAmount(new BigDecimal("200.00"));

        mockMvc.perform(post("/api/accounts/transfer")
                        .with(httpBasic(testUser.getEmail(), TEST_PASSWORD))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(transferTransaction)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.accountDetails[0].currentBalance").value("1798.00"));  // 2000 - 202

        Account updatedSource = accountRepository.findById(creditAccount1.getId()).orElseThrow();
        Account updatedTarget = accountRepository.findById(creditAccount2.getId()).orElseThrow();

        assertEquals(new BigDecimal("1798.00"), updatedSource.getBalance());
        assertEquals(new BigDecimal("1200.00"), updatedTarget.getBalance());
    }

    @Test
    void transfer_WhenInsufficientBalance_Fail() throws Exception {
        TransferTransaction transferTransaction = new TransferTransaction();
        transferTransaction.setSourceAccountId(creditAccount1.getId());
        transferTransaction.setTargetAccountId(creditAccount2.getId());
        transferTransaction.setAmount(new BigDecimal("5500"));

        mockMvc.perform(post("/api/accounts/transfer")
                        .with(httpBasic(testUser.getEmail(), TEST_PASSWORD))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(transferTransaction)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value(
                        "Cannot subtract 5555.00. Current balance: 2000.00. Resulting balance would be negative."));
    }

    @Test
    void transfer_WhenTargetAccountNotFound_Fail() throws Exception {
        TransferTransaction transferTransaction = new TransferTransaction();
        transferTransaction.setSourceAccountId(creditAccount1.getId());
        transferTransaction.setTargetAccountId(999L);
        transferTransaction.setAmount(new BigDecimal("100.00"));

        mockMvc.perform(post("/api/accounts/transfer")
                        .with(httpBasic(testUser.getEmail(), TEST_PASSWORD))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(transferTransaction)))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").value("Target account not found"));
    }

}
