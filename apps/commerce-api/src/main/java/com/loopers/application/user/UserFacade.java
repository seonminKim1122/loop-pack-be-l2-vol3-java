package com.loopers.application.user;

import com.loopers.domain.user.User;
import com.loopers.domain.user.UserRepository;
import com.loopers.domain.user.UserService;
import com.loopers.domain.user.vo.LoginId;
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
}
