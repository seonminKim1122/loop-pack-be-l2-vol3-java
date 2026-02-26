package com.loopers.domain.order;

import com.loopers.support.error.CoreException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

class OrderTest {

    @DisplayName("Order 를 생성할 때, ")
    @Nested
    class Create {

        @DisplayName("유효한 사용자와 주문 항목이 주어지면, 총 주문 금액이 계산되어 생성된다.")
        @Test
        void createsOrder_withCalculatedTotalPrice() {
            // arrange
            Long userId = 1L;
            List<OrderItem> items = List.of(
                OrderItem.of(1L, "나이키 에어맥스", "나이키", 150000, 2),
                OrderItem.of(2L, "아디다스 슈퍼스타", "아디다스", 100000, 1)
            );

            // act
            Order order = Order.of(userId, items);

            // assert
            assertThat(order.totalPrice()).isEqualTo(400000); // 150000*2 + 100000*1
        }

        @DisplayName("userId 가 null 이면, CoreException 이 발생한다.")
        @Test
        void throwsCoreException_whenUserIdIsNull() {
            // arrange
            List<OrderItem> items = List.of(
                OrderItem.of(1L, "나이키 에어맥스", "나이키", 150000, 1)
            );

            // act
            CoreException result = assertThrows(CoreException.class, () ->
                Order.of(null, items)
            );

            // assert
            assertThat(result.getCustomMessage()).isEqualTo("사용자 ID는 필수입니다.");
        }
    }
}
