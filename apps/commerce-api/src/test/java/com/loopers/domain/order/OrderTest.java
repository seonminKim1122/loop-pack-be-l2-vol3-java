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

        @DisplayName("유효한 사용자와 주문 항목이 주어지면, 쿠폰 적용 전 금액이 계산되어 생성된다.")
        @Test
        void createsOrder_withCalculatedOriginalAmount() {
            // arrange
            Long userId = 1L;
            List<OrderItem> items = List.of(
                OrderItem.of(1L, "나이키 에어맥스", "나이키", 150000, 2),
                OrderItem.of(2L, "아디다스 슈퍼스타", "아디다스", 100000, 1)
            );

            // act
            Order order = Order.of(userId, items);

            // assert
            assertThat(order.originalAmount()).isEqualTo(400000L); // 150000*2 + 100000*1
        }

        @DisplayName("쿠폰이 없으면, discountAmount 는 0 이고 paymentAmount 는 originalAmount 와 같다.")
        @Test
        void createsOrder_withNoDiscount_whenNoCouponProvided() {
            // arrange
            Long userId = 1L;
            List<OrderItem> items = List.of(
                OrderItem.of(1L, "나이키 에어맥스", "나이키", 150000, 2)
            );

            // act
            Order order = Order.of(userId, items);

            // assert
            assertThat(order.discountAmount()).isEqualTo(0L);
            assertThat(order.paymentAmount()).isEqualTo(order.originalAmount());
            assertThat(order.issuedCouponId()).isNull();
        }

        @DisplayName("쿠폰이 적용되면, discountAmount 와 paymentAmount 가 올바르게 셋팅된다.")
        @Test
        void createsOrder_withCoupon_setsDiscountAndPaymentAmount() {
            // arrange
            Long userId = 1L;
            List<OrderItem> items = List.of(
                OrderItem.of(1L, "나이키 에어맥스", "나이키", 150000, 2),
                OrderItem.of(2L, "아디다스 슈퍼스타", "아디다스", 100000, 1)
            );
            Long issuedCouponId = 1L;
            long discountAmount = 3000L;

            // act
            Order order = Order.of(userId, items, issuedCouponId, discountAmount);

            // assert
            assertThat(order.originalAmount()).isEqualTo(400000L);
            assertThat(order.discountAmount()).isEqualTo(3000L);
            assertThat(order.paymentAmount()).isEqualTo(397000L);
            assertThat(order.issuedCouponId()).isEqualTo(1L);
        }

        @DisplayName("할인 금액이 주문 금액을 초과하면, paymentAmount 는 0 이 된다.")
        @Test
        void setsPaymentAmountToZero_whenDiscountExceedsOriginalAmount() {
            // arrange
            Long userId = 1L;
            List<OrderItem> items = List.of(
                OrderItem.of(1L, "나이키 에어맥스", "나이키", 1000, 1)
            );
            Long issuedCouponId = 1L;
            long discountAmount = 3000L;

            // act
            Order order = Order.of(userId, items, issuedCouponId, discountAmount);

            // assert
            assertThat(order.paymentAmount()).isEqualTo(0L);
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

        @DisplayName("items 가 null 이면, CoreException 이 발생한다.")
        @Test
        void throwsCoreException_whenItemsIsNull() {
            // arrange
            Long userId = 1L;

            // act
            CoreException result = assertThrows(CoreException.class, () ->
                Order.of(userId, null)
            );

            // assert
            assertThat(result.getCustomMessage()).isEqualTo("주문 항목은 필수입니다.");
        }

        @DisplayName("items 가 비어있으면, CoreException 이 발생한다.")
        @Test
        void throwsCoreException_whenItemsIsEmpty() {
            // arrange
            Long userId = 1L;

            // act
            CoreException result = assertThrows(CoreException.class, () ->
                Order.of(userId, List.of())
            );

            // assert
            assertThat(result.getCustomMessage()).isEqualTo("주문 항목이 비어있습니다.");
        }
    }
}
