package com.rabobank.bankservice.model;

import lombok.Builder;
import lombok.Data;

import java.util.List;

@Data
@Builder
public class UserAccount {
    private String userName;
    private String userEmail;

    private List<AccountDetail> accountDetails;
}
