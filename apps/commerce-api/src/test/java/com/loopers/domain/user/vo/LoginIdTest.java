package com.loopers.domain.user.vo;

import com.loopers.support.error.CoreException;
import com.loopers.support.error.ErrorType;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

class LoginIdTest {

    @DisplayName("LoginId 를 생성할 때, ")
    @Nested
    class Create {

        @DisplayName("영문과 숫자로 이루어진 6~20자 값이 주어지면, 정상적으로 생성된다.")
        @Test
        void createsLoginId_whenValueIsValid() {
            // arrange
            String value = "testUser1";

            // act
            LoginId loginId = LoginId.from(value);

            // assert
            assertThat(loginId).isNotNull();
        }

        @DisplayName("null 이 주어지면, BAD_REQUEST 예외가 발생한다.")
        @Test
        void throwsBadRequestException_whenValueIsNull() {
            // arrange
            String value = null;

            // act
            CoreException result = assertThrows(CoreException.class, () -> LoginId.from(value));

            // assert
            assertThat(result.getErrorType()).isEqualTo(ErrorType.BAD_REQUEST);
        }

        @DisplayName("6자 미만이면, BAD_REQUEST 예외가 발생한다.")
        @Test
        void throwsBadRequestException_whenValueIsTooShort() {
            // arrange
            String value = "abc1";

            // act
            CoreException result = assertThrows(CoreException.class, () -> LoginId.from(value));

            // assert
            assertThat(result.getErrorType()).isEqualTo(ErrorType.BAD_REQUEST);
        }

        @DisplayName("20자 초과이면, BAD_REQUEST 예외가 발생한다.")
        @Test
        void throwsBadRequestException_whenValueIsTooLong() {
            // arrange
            String value = "abcdefghijklmnopqrstu";

            // act
            CoreException result = assertThrows(CoreException.class, () -> LoginId.from(value));

            // assert
            assertThat(result.getErrorType()).isEqualTo(ErrorType.BAD_REQUEST);
        }

        @DisplayName("특수문자가 포함되면, BAD_REQUEST 예외가 발생한다.")
        @Test
        void throwsBadRequestException_whenValueContainsSpecialCharacter() {
            // arrange
            String value = "testUser1!";

            // act
            CoreException result = assertThrows(CoreException.class, () -> LoginId.from(value));

            // assert
            assertThat(result.getErrorType()).isEqualTo(ErrorType.BAD_REQUEST);
        }
    }
}
