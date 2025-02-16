package com.rabobank.bankservice.model;

import com.rabobank.bankservice.entity.Account;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Named;

import java.util.List;

@Mapper(componentModel = "spring")
public interface AccountToUserAccountMapper {

    @Named("accountsToUserAccount")
    default UserAccount accountsToUserAccount(List<Account> accounts) {
        if (accounts == null || accounts.isEmpty()) {
            return null;
        }

        Account firstAccount = accounts.get(0);

        return UserAccount.builder()
                .userName(firstAccount.getUser().getName())
                .userEmail(firstAccount.getUser().getEmail())
                .accountDetails(accounts.stream()
                        .map(this::mapToAccountDetail)
                        .toList())
                .build();
    }

    @Mapping(source = "balance", target = "currentBalance")
    @Mapping(source = "card.cardNumber", target = "cardNumber")
    @Mapping(source = "card.cardType", target = "cardType")
    AccountDetail mapToAccountDetail(Account account);
}
