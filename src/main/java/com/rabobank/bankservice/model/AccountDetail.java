package com.rabobank.bankservice.model;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.rabobank.bankservice.entity.CardType;
import com.rabobank.bankservice.serializer.BigDecimalSerializer;
import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;

@Data
@Builder
public class AccountDetail {

    private String cardNumber;
    private CardType cardType;

    @JsonSerialize(using = BigDecimalSerializer.class)
    private BigDecimal currentBalance;
}
