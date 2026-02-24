package com.loopers.domain.user;

import com.loopers.domain.user.vo.BirthDate;
import com.loopers.support.error.CoreException;
import com.loopers.support.error.ErrorType;
import org.springframework.stereotype.Component;

import java.time.format.DateTimeFormatter;
import java.util.regex.Pattern;

@Component
public class PasswordValidator {

    private static final Pattern PASSWORD_PATTERN = Pattern.compile("^[0-9a-zA-Z!@#$%^&*]{8,16}$");
    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyyMMdd");

    public void validate(String rawPassword, BirthDate birthDate) {
        if (!PASSWORD_PATTERN.matcher(rawPassword).matches()) {
            throw new CoreException(ErrorType.BAD_REQUEST, "비밀번호 형식이 올바르지 않습니다.");
        }

        String formattedBirthDate = DATE_FORMATTER.format(birthDate.value());
        if (rawPassword.contains(formattedBirthDate)) {
            throw new CoreException(ErrorType.BAD_REQUEST, "비밀번호에 생년월일을 포함할 수 없습니다.");
        }
    }
}
