package com.rabobank.bankservice.model.request;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.Data;

import java.math.BigDecimal;

@Data
public class TransferTransaction implements BalanceTransaction {

    @NotNull(message = "Source account ID is required")
    private Long sourceAccountId;

    @NotNull(message = "Target account ID is required")
    private Long targetAccountId;

    @NotNull(message = "Amount is required")
    @Positive(message = "Transfer amount must be positive")
    private BigDecimal amount;
}
