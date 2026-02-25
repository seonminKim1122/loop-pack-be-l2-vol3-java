package com.loopers.domain.user;

import com.loopers.support.error.CoreException;
import com.loopers.support.error.ErrorType;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@RequiredArgsConstructor
@Component
public class UserService {

    private final PasswordEncoder passwordEncoder;
    private final PasswordValidator passwordValidator;

    public void authenticate(User user, String rawPassword) {
        if (!passwordEncoder.matches(rawPassword, user.password())) {
            throw new CoreException(ErrorType.UNAUTHORIZED, "회원 정보가 올바르지 않습니다.");
        }
    }

    public void changePassword(User user, String newPassword) {
        if (passwordEncoder.matches(newPassword, user.password())) {
            throw new CoreException(ErrorType.BAD_REQUEST, "현재 비밀번호와 동일한 비밀번호로 변경할 수 없습니다.");
        }
        passwordValidator.validate(newPassword, user.birthDate());
        user.changePassword(passwordEncoder.encode(newPassword));
    }
}
