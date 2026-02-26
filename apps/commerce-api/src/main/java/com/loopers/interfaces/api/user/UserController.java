package com.loopers.interfaces.api.user;

import com.loopers.application.user.UserFacade;
import com.loopers.application.user.UserInfo;
import com.loopers.interfaces.api.ApiResponse;
import com.loopers.interfaces.auth.AuthenticatedUser;
import com.loopers.interfaces.auth.CurrentUser;
import com.loopers.interfaces.auth.LoginRequired;
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

    @LoginRequired
    @GetMapping("/me")
    public ApiResponse<UserDto.MyInfoResponse> getMyInfo(@CurrentUser AuthenticatedUser authUser) {
        UserInfo userInfo = userFacade.getMyInfo(authUser.id());
        return ApiResponse.success(UserDto.MyInfoResponse.from(userInfo));
    }

    @LoginRequired
    @PutMapping("/me/password")
    public ApiResponse<Void> changePassword(
            @CurrentUser AuthenticatedUser authUser,
            @RequestBody UserDto.ChangePasswordRequest request) {
        userFacade.changePassword(authUser.id(), request.newPassword());
        return ApiResponse.success(null);
    }
}
