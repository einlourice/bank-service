package com.rabobank.bankservice.model.request;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.Data;

import java.math.BigDecimal;

@Data
public class WithdrawTransaction implements BalanceTransaction {

    @NotNull(message = "Amount is required")
    @Positive(message = "Transfer amount must be positive")
    private BigDecimal amount;

    @NotNull(message = "Account ID is required")
    private Long account;
}
