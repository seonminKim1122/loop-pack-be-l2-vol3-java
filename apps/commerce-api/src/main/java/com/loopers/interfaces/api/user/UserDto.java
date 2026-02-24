package com.loopers.interfaces.api.user;

import java.time.LocalDate;

public class UserDto {

    public static record SignupRequest(String loginId, String password, String name, LocalDate birthDate, String email){

    }
}
