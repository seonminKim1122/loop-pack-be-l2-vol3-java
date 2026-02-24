package com.loopers.interfaces.api.user;

import com.loopers.application.user.UserFacade;
import com.loopers.interfaces.api.ApiResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

@RequiredArgsConstructor
@RestController
@RequestMapping("/api/v1/users")
public class UserController {

    private final UserFacade userFacade;

    @ResponseStatus(HttpStatus.CREATED)
    @PostMapping
    public ApiResponse<Void> signup(@RequestBody UserDto.SignupRequest request) {

        userFacade.signup(request.loginId(),
                          request.password(),
                          request.name(),
                          request.birthDate(),
                          request.email());

        return ApiResponse.success(null);
    }
}
