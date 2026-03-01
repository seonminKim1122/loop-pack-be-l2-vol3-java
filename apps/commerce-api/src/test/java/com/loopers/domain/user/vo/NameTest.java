package com.loopers.domain.user.vo;

import com.loopers.support.error.CoreException;
import com.loopers.support.error.ErrorType;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

class NameTest {

    @DisplayName("Name 을 생성할 때, ")
    @Nested
    class Create {

        @DisplayName("한글 2~6자 값이 주어지면, 정상적으로 생성된다.")
        @Test
        void createsName_whenValueIsValid() {
            // arrange
            String value = "홍길동";

            // act
            Name name = Name.from(value);

            // assert
            assertThat(name).isNotNull();
        }

        @DisplayName("null 이 주어지면, BAD_REQUEST 예외가 발생한다.")
        @Test
        void throwsBadRequestException_whenValueIsNull() {
            // arrange
            String value = null;

            // act
            CoreException result = assertThrows(CoreException.class, () -> Name.from(value));

            // assert
            assertThat(result.getErrorType()).isEqualTo(ErrorType.BAD_REQUEST);
        }

        @DisplayName("2자 미만이면, BAD_REQUEST 예외가 발생한다.")
        @Test
        void throwsBadRequestException_whenValueIsTooShort() {
            // arrange
            String value = "홍";

            // act
            CoreException result = assertThrows(CoreException.class, () -> Name.from(value));

            // assert
            assertThat(result.getErrorType()).isEqualTo(ErrorType.BAD_REQUEST);
        }

        @DisplayName("6자 초과이면, BAD_REQUEST 예외가 발생한다.")
        @Test
        void throwsBadRequestException_whenValueIsTooLong() {
            // arrange
            String value = "홍길동김철수박";

            // act
            CoreException result = assertThrows(CoreException.class, () -> Name.from(value));

            // assert
            assertThat(result.getErrorType()).isEqualTo(ErrorType.BAD_REQUEST);
        }

        @DisplayName("한글이 아닌 문자가 포함되면, BAD_REQUEST 예외가 발생한다.")
        @Test
        void throwsBadRequestException_whenValueContainsNonKorean() {
            // arrange
            String value = "홍gil동";

            // act
            CoreException result = assertThrows(CoreException.class, () -> Name.from(value));

            // assert
            assertThat(result.getErrorType()).isEqualTo(ErrorType.BAD_REQUEST);
        }
    }
}
