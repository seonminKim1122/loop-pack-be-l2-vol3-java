package com.loopers.domain.order;

import com.loopers.support.error.CoreException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

class OrderItemTest {

    @DisplayName("OrderItem 을 생성할 때, ")
    @Nested
    class Create {

        @DisplayName("모든 필드가 유효하면, 정상적으로 생성된다.")
        @Test
        void createsOrderItem_whenAllFieldsAreValid() {
            // arrange
            Long productId = 1L;
            String productName = "나이키 에어맥스";
            String brandName = "나이키";
            Integer unitPrice = 150000;
            Integer quantity = 2;

            // act
            OrderItem orderItem = OrderItem.of(productId, productName, brandName, unitPrice, quantity);

            // assert
            assertThat(orderItem).isNotNull();
        }

        @DisplayName("수량이 0이면, CoreException 이 발생한다.")
        @Test
        void throwsCoreException_whenQuantityIsZero() {
            // arrange & act
            CoreException result = assertThrows(CoreException.class, () ->
                OrderItem.of(1L, "나이키 에어맥스", "나이키", 150000, 0)
            );

            // assert
            assertThat(result.getCustomMessage()).isEqualTo("주문 수량이 올바르지 않습니다.");
        }

        @DisplayName("수량이 음수이면, CoreException 이 발생한다.")
        @Test
        void throwsCoreException_whenQuantityIsNegative() {
            // arrange & act
            CoreException result = assertThrows(CoreException.class, () ->
                OrderItem.of(1L, "나이키 에어맥스", "나이키", 150000, -1)
            );

            // assert
            assertThat(result.getCustomMessage()).isEqualTo("주문 수량이 올바르지 않습니다.");
        }
    }
}
