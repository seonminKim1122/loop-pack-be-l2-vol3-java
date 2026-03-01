package com.loopers.domain.user.vo;

import com.loopers.support.error.CoreException;
import com.loopers.support.error.ErrorType;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

class BirthDateTest {

    @DisplayName("BirthDate 를 생성할 때, ")
    @Nested
    class Create {

        @DisplayName("과거 날짜가 주어지면, 정상적으로 생성된다.")
        @Test
        void createsBirthDate_whenValueIsValid() {
            // arrange
            LocalDate value = LocalDate.of(1990, 1, 1);

            // act
            BirthDate birthDate = BirthDate.from(value);

            // assert
            assertThat(birthDate).isNotNull();
        }

        @DisplayName("null 이 주어지면, BAD_REQUEST 예외가 발생한다.")
        @Test
        void throwsBadRequestException_whenValueIsNull() {
            // arrange
            LocalDate value = null;

            // act
            CoreException result = assertThrows(CoreException.class, () -> BirthDate.from(value));

            // assert
            assertThat(result.getErrorType()).isEqualTo(ErrorType.BAD_REQUEST);
        }

        @DisplayName("미래 날짜가 주어지면, BAD_REQUEST 예외가 발생한다.")
        @Test
        void throwsBadRequestException_whenValueIsInFuture() {
            // arrange
            LocalDate value = LocalDate.now().plusDays(1);

            // act
            CoreException result = assertThrows(CoreException.class, () -> BirthDate.from(value));

            // assert
            assertThat(result.getErrorType()).isEqualTo(ErrorType.BAD_REQUEST);
        }
    }
}
