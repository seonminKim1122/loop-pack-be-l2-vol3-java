package com.loopers.application.user;

import com.loopers.domain.user.User;

import java.time.LocalDate;

public record UserInfo(String loginId, String maskedName, LocalDate birthDate, String email) {

    public static UserInfo from(User user) {
        String name = user.name().value();
        String maskedName = name.substring(0, name.length() - 1) + "*";
        return new UserInfo(
            user.loginId().value(),
            maskedName,
            user.birthDate().value(),
            user.email().value()
        );
    }
}
