package com.loopers.domain.user;

import com.loopers.domain.user.vo.BirthDate;
import com.loopers.domain.user.vo.Email;
import com.loopers.domain.user.vo.LoginId;
import com.loopers.domain.user.vo.Name;
import com.loopers.support.error.CoreException;
import com.loopers.support.error.ErrorType;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.util.Optional;

@RequiredArgsConstructor
@Component
public class UserService {

    private final PasswordEncoder passwordEncoder;
    private final PasswordValidator passwordValidator;

    public User signup(Optional<User> duplicateUser, String loginId, String password, String name, LocalDate birthDate, String email) {
        if (duplicateUser.isPresent()) {
            throw new CoreException(ErrorType.CONFLICT, "이미 등록된 로그인ID 입니다.");
        }

        LoginId loginIdVo = LoginId.from(loginId);
        BirthDate birthDateVo = BirthDate.from(birthDate);
        passwordValidator.validate(password, birthDateVo);
        String encodedPassword = passwordEncoder.encode(password);
        Name nameVo = Name.from(name);
        Email emailVo = Email.from(email);

        return User.create(loginIdVo, encodedPassword, nameVo, birthDateVo, emailVo);
    }

    public void authenticate(Optional<User> foundUser, String rawPassword) {
        User user = foundUser.orElseThrow(
            () -> new CoreException(ErrorType.UNAUTHORIZED, "회원 정보가 올바르지 않습니다.")
        );
        if (!passwordEncoder.matches(rawPassword, user.password())) {
            throw new CoreException(ErrorType.UNAUTHORIZED, "회원 정보가 올바르지 않습니다.");
        }
    }

    public void changePassword(User user, String newPassword) {
        if (passwordEncoder.matches(newPassword, user.password())) {
            throw new CoreException(ErrorType.BAD_REQUEST, "현재 비밀번호와 동일한 비밀번호로 변경할 수 없습니다.");
        }
        passwordValidator.validate(newPassword, user.birthDate());
        String encodedNewPassword = passwordEncoder.encode(newPassword);
        user.changePassword(encodedNewPassword);
    }
}
