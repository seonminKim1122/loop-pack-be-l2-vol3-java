package com.loopers.domain.user;

import com.loopers.domain.user.vo.BirthDate;
import com.loopers.support.error.CoreException;
import com.loopers.support.error.ErrorType;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class UserServiceTest {

    PasswordEncoder passwordEncoder = mock(PasswordEncoder.class);
    PasswordValidator passwordValidator = mock(PasswordValidator.class);
    UserService userService = new UserService(passwordEncoder, passwordValidator);

    @DisplayName("인증 시, ")
    @Nested
    class Authenticate {

        @DisplayName("비밀번호가 일치하지 않으면, UNAUTHORIZED 예외가 발생한다.")
        @Test
        void throwsUnauthorizedException_whenPasswordDoesNotMatch() {
            // arrange
            User user = mock(User.class);
            when(user.password()).thenReturn("encodedPassword");
            when(passwordEncoder.matches("wrongPassword", "encodedPassword")).thenReturn(false);

            // act
            CoreException result = assertThrows(CoreException.class, () ->
                userService.authenticate(user, "wrongPassword")
            );

            // assert
            assertThat(result.getErrorType()).isEqualTo(ErrorType.UNAUTHORIZED);
        }

        @DisplayName("비밀번호가 일치하면, 예외가 발생하지 않는다.")
        @Test
        void doesNotThrow_whenPasswordMatches() {
            // arrange
            User user = mock(User.class);
            when(user.password()).thenReturn("encodedPassword");
            when(passwordEncoder.matches("test1234!", "encodedPassword")).thenReturn(true);

            // act & assert
            assertDoesNotThrow(() -> userService.authenticate(user, "test1234!"));
        }
    }

    @DisplayName("비밀번호 수정 시, ")
    @Nested
    class ChangePassword {

        @DisplayName("새 비밀번호가 현재 비밀번호와 동일하면, BAD_REQUEST 예외가 발생한다.")
        @Test
        void throwsBadRequestException_whenNewPasswordIsSameAsCurrent() {
            // arrange
            User user = mock(User.class);
            when(user.password()).thenReturn("encodedPassword");
            when(passwordEncoder.matches("test1234!", "encodedPassword")).thenReturn(true);

            // act
            CoreException result = assertThrows(CoreException.class, () ->
                userService.changePassword(user, "test1234!")
            );

            // assert
            assertThat(result.getErrorType()).isEqualTo(ErrorType.BAD_REQUEST);
        }

        @DisplayName("유효한 새 비밀번호이면, 비밀번호가 변경된다.")
        @Test
        void changesPassword_whenNewPasswordIsValid() {
            // arrange
            User user = mock(User.class);
            when(user.password()).thenReturn("encodedPassword");
            when(user.birthDate()).thenReturn(BirthDate.from(LocalDate.of(1990, 1, 1)));
            when(passwordEncoder.matches("newPass1!", "encodedPassword")).thenReturn(false);
            when(passwordEncoder.encode("newPass1!")).thenReturn("newEncodedPassword");

            // act
            userService.changePassword(user, "newPass1!");

            // assert
            verify(user).changePassword("newEncodedPassword");
        }
    }
}
