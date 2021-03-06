package com.beerjournal.breweriana.user.account.dto;

import lombok.Builder;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import org.hibernate.validator.constraints.NotEmpty;

import static lombok.AccessLevel.PRIVATE;

@Data
@Builder
@RequiredArgsConstructor(access = PRIVATE)
public class AccountChangePasswordDto {
    @NotEmpty private final String password;
    @NotEmpty private final String newPassword;
}
