package com.loopers.application.user;

import com.loopers.domain.user.PasswordEncoder;
import com.loopers.domain.user.PasswordValidator;
import com.loopers.domain.user.User;
import com.loopers.domain.user.UserRepository;
import com.loopers.domain.user.UserService;
import com.loopers.domain.user.vo.BirthDate;
import com.loopers.domain.user.vo.Email;
import com.loopers.domain.user.vo.LoginId;
import com.loopers.domain.user.vo.Name;
import com.loopers.support.error.CoreException;
import com.loopers.support.error.ErrorType;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;

@RequiredArgsConstructor
@Component
public class UserFacade {

    private final UserRepository userRepository;
    private final UserService userService;
    private final PasswordEncoder passwordEncoder;
    private final PasswordValidator passwordValidator;

    @Transactional
    public void signup(String loginId, String password, String name, LocalDate birthDate, String email) {
        LoginId loginIdVo = LoginId.from(loginId);
        if (userRepository.findByLoginId(loginIdVo).isPresent()) {
            throw new CoreException(ErrorType.CONFLICT, "이미 등록된 로그인ID 입니다.");
        }

        BirthDate birthDateVo = BirthDate.from(birthDate);
        passwordValidator.validate(password, birthDateVo);
        String encodedPassword = passwordEncoder.encode(password);

        userRepository.save(User.create(loginIdVo, encodedPassword, Name.from(name), birthDateVo, Email.from(email)));
    }

    public void authenticate(String loginId, String rawPassword) {
        User user = userRepository.findByLoginId(LoginId.from(loginId))
            .orElseThrow(() -> new CoreException(ErrorType.UNAUTHORIZED, "회원 정보가 올바르지 않습니다."));
        userService.authenticate(user, rawPassword);
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
