package com.loopers.interfaces.api.user;

import com.loopers.application.user.UserInfo;

import java.time.LocalDate;

public class UserDto {

    public static record SignupRequest(String loginId, String password, String name, LocalDate birthDate, String email) {
    }

    public static record MyInfoResponse(String loginId, String name, LocalDate birthDate, String email) {

        public static MyInfoResponse from(UserInfo userInfo) {
            return new MyInfoResponse(
                userInfo.loginId(),
                userInfo.maskedName(),
                userInfo.birthDate(),
                userInfo.email()
            );
        }
    }
}
