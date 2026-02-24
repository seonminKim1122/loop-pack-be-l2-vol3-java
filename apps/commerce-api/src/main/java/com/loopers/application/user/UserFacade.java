package com.loopers.application.user;

import com.loopers.domain.user.User;
import com.loopers.domain.user.UserRepository;
import com.loopers.domain.user.UserService;
import com.loopers.domain.user.vo.LoginId;
import com.loopers.support.error.CoreException;
import com.loopers.support.error.ErrorType;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.Optional;

@RequiredArgsConstructor
@Component
public class UserFacade {

    private final UserRepository userRepository;
    private final UserService userService;

    @Transactional
    public void signup(String loginId, String password, String name, LocalDate birthDate, String email) {
        Optional<User> duplicateUser = userRepository.findByLoginId(LoginId.from(loginId));
        User newUser = userService.signup(duplicateUser, loginId, password, name, birthDate, email);
        userRepository.save(newUser);
    }

    public void authenticate(String loginId, String rawPassword) {
        Optional<User> foundUser = userRepository.findByLoginId(LoginId.from(loginId));
        userService.authenticate(foundUser, rawPassword);
    }

    @Transactional(readOnly = true)
    public UserInfo getMyInfo(String loginId) {
        return userRepository.findByLoginId(LoginId.from(loginId))
            .map(UserInfo::from)
            .orElseThrow(() -> new CoreException(ErrorType.NOT_FOUND, "존재하지 않는 회원입니다."));
    }

    @Transactional
    public void changePassword(String loginId, String newPassword) {
        User user = userRepository.findByLoginId(LoginId.from(loginId))
            .orElseThrow(() -> new CoreException(ErrorType.NOT_FOUND, "존재하지 않는 회원입니다."));
        userService.changePassword(user, newPassword);
    }
}
