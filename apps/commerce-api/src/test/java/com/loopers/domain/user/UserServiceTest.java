package com.loopers.domain.user;

import com.loopers.support.error.CoreException;
import com.loopers.support.error.ErrorType;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class UserServiceTest {

    PasswordEncoder passwordEncoder = mock(PasswordEncoder.class);
    PasswordValidator passwordValidator = mock(PasswordValidator.class);
    UserService userService = new UserService(passwordEncoder, passwordValidator);

    @DisplayName("회원가입 시, ")
    @Nested
    class Signup {

        @DisplayName("중복되지 않은 loginId 로 가입하면, User 를 반환한다.")
        @Test
        void returnsUser_whenLoginIdIsNotDuplicated() {
            // arrange
            when(passwordEncoder.encode(any())).thenReturn("encodedPassword");

            // act
            User user = userService.signup(
                Optional.empty(),
                "testUser1", "test1234!", "홍길동", LocalDate.of(1990, 1, 1), "test@loopers.im"
            );

            // assert
            assertThat(user).isNotNull();
        }

        @DisplayName("이미 존재하는 loginId 로 가입하면, CONFLICT 예외가 발생한다.")
        @Test
        void throwsConflictException_whenLoginIdIsDuplicated() {
            // arrange
            User existingUser = mock(User.class);

            // act
            CoreException result = assertThrows(CoreException.class, () ->
                userService.signup(
                    Optional.of(existingUser),
                    "testUser1", "test1234!", "홍길동", LocalDate.of(1990, 1, 1), "test@loopers.im"
                )
            );

            // assert
            assertThat(result.getErrorType()).isEqualTo(ErrorType.CONFLICT);
        }
    }

    @DisplayName("인증 시, ")
    @Nested
    class Authenticate {

        @DisplayName("존재하지 않는 User 로 인증하면, UNAUTHORIZED 예외가 발생한다.")
        @Test
        void throwsUnauthorizedException_whenUserNotFound() {
            // act
            CoreException result = assertThrows(CoreException.class, () ->
                userService.authenticate(Optional.empty(), "test1234!")
            );

            // assert
            assertThat(result.getErrorType()).isEqualTo(ErrorType.UNAUTHORIZED);
        }

        @DisplayName("비밀번호가 일치하지 않으면, UNAUTHORIZED 예외가 발생한다.")
        @Test
        void throwsUnauthorizedException_whenPasswordDoesNotMatch() {
            // arrange
            User user = mock(User.class);
            when(user.password()).thenReturn("encodedPassword");
            when(passwordEncoder.matches("wrongPassword", "encodedPassword")).thenReturn(false);

            // act
            CoreException result = assertThrows(CoreException.class, () ->
                userService.authenticate(Optional.of(user), "wrongPassword")
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
            assertDoesNotThrow(() -> userService.authenticate(Optional.of(user), "test1234!"));
        }
    }
}
