package com.loopers.domain.user.vo;

import com.loopers.support.error.CoreException;
import com.loopers.support.error.ErrorType;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

class EmailTest {

    @DisplayName("Email 을 생성할 때, ")
    @Nested
    class Create {

        @DisplayName("올바른 이메일 형식이 주어지면, 정상적으로 생성된다.")
        @Test
        void createsEmail_whenValueIsValid() {
            // arrange
            String value = "test@loopers.im";

            // act
            Email email = Email.from(value);

            // assert
            assertThat(email).isNotNull();
        }

        @DisplayName("null 이 주어지면, BAD_REQUEST 예외가 발생한다.")
        @Test
        void throwsBadRequestException_whenValueIsNull() {
            // arrange
            String value = null;

            // act
            CoreException result = assertThrows(CoreException.class, () -> Email.from(value));

            // assert
            assertThat(result.getErrorType()).isEqualTo(ErrorType.BAD_REQUEST);
        }

        @DisplayName("@ 가 없으면, BAD_REQUEST 예외가 발생한다.")
        @Test
        void throwsBadRequestException_whenValueHasNoAtSign() {
            // arrange
            String value = "testloopers.im";

            // act
            CoreException result = assertThrows(CoreException.class, () -> Email.from(value));

            // assert
            assertThat(result.getErrorType()).isEqualTo(ErrorType.BAD_REQUEST);
        }

        @DisplayName("도메인이 없으면, BAD_REQUEST 예외가 발생한다.")
        @Test
        void throwsBadRequestException_whenValueHasNoDomain() {
            // arrange
            String value = "test@";

            // act
            CoreException result = assertThrows(CoreException.class, () -> Email.from(value));

            // assert
            assertThat(result.getErrorType()).isEqualTo(ErrorType.BAD_REQUEST);
        }
    }
}
