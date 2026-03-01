package com.loopers.domain.product.vo;

import com.loopers.support.error.CoreException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

class PriceTest {

    @DisplayName("Price 를 생성할 때, ")
    @Nested
    class Create {

        @DisplayName("자연수 값이 주어지면, 정상적으로 생성된다.")
        @Test
        void createsPrice_whenValueIsPositive() {
            // arrange
            int value = 1000;

            // act
            Price price = Price.from(value);

            // assert
            assertThat(price).isNotNull();
            assertThat(price.value()).isEqualTo(value);
        }

        @DisplayName("0 이 주어지면, CoreException 이 발생한다.")
        @Test
        void throwsCoreException_whenValueIsZero() {
            // arrange
            int value = 0;

            // act
            CoreException result = assertThrows(CoreException.class, () -> Price.from(value));

            // assert
            assertThat(result.getCustomMessage()).isEqualTo("가격은 0원 이상이어야 합니다.");
        }

        @DisplayName("음수가 주어지면, CoreException 이 발생한다.")
        @Test
        void throwsCoreException_whenValueIsNegative() {
            // arrange
            int value = -1;

            // act
            CoreException result = assertThrows(CoreException.class, () -> Price.from(value));

            // assert
            assertThat(result.getCustomMessage()).isEqualTo("가격은 0원 이상이어야 합니다.");
        }
    }
}
