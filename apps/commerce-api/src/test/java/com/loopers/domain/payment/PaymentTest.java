package com.loopers.domain.payment;

import com.loopers.support.error.CoreException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

class PaymentTest {

    @DisplayName("Payment 를 생성할 때, ")
    @Nested
    class Create {

        @DisplayName("유효한 값이 주어지면, status 가 PENDING 으로 생성된다.")
        @Test
        void createsPayment_withPendingStatus() {
            // arrange
            String orderId = "20260318-ABCD12";
            Long userId = 1L;
            String cardType = "신한카드";
            String cardNo = "1234-5678-9012-3456";
            Long amount = 50000L;

            // act
            Payment payment = Payment.of(orderId, userId, cardType, cardNo, amount);

            // assert
            assertThat(payment.status()).isEqualTo(PaymentStatus.PENDING);
        }

        @DisplayName("orderId 가 null 이면, CoreException 이 발생한다.")
        @Test
        void throwsCoreException_whenOrderIdIsNull() {
            // arrange & act
            CoreException result = assertThrows(CoreException.class, () ->
                Payment.of(null, 1L, "신한카드", "1234-5678-9012-3456", 50000L)
            );

            // assert
            assertThat(result.getCustomMessage()).isEqualTo("주문번호는 비어있을 수 없습니다.");
        }

        @DisplayName("orderId 가 blank 이면, CoreException 이 발생한다.")
        @Test
        void throwsCoreException_whenOrderIdIsBlank() {
            // arrange & act
            CoreException result = assertThrows(CoreException.class, () ->
                Payment.of("  ", 1L, "신한카드", "1234-5678-9012-3456", 50000L)
            );

            // assert
            assertThat(result.getCustomMessage()).isEqualTo("주문번호는 비어있을 수 없습니다.");
        }

        @DisplayName("userId 가 null 이면, CoreException 이 발생한다.")
        @Test
        void throwsCoreException_whenUserIdIsNull() {
            // arrange & act
            CoreException result = assertThrows(CoreException.class, () ->
                Payment.of("20260318-ABCD12", null, "신한카드", "1234-5678-9012-3456", 50000L)
            );

            // assert
            assertThat(result.getCustomMessage()).isEqualTo("결제자ID는 비어있을 수 없습니다.");
        }

        @DisplayName("cardType 이 null 이면, CoreException 이 발생한다.")
        @Test
        void throwsCoreException_whenCardTypeIsNull() {
            // arrange & act
            CoreException result = assertThrows(CoreException.class, () ->
                Payment.of("20260318-ABCD12", 1L, null, "1234-5678-9012-3456", 50000L)
            );

            // assert
            assertThat(result.getCustomMessage()).isEqualTo("카드타입은 비어있을 수 없습니다.");
        }

        @DisplayName("cardType 이 blank 이면, CoreException 이 발생한다.")
        @Test
        void throwsCoreException_whenCardTypeIsBlank() {
            // arrange & act
            CoreException result = assertThrows(CoreException.class, () ->
                Payment.of("20260318-ABCD12", 1L, "  ", "1234-5678-9012-3456", 50000L)
            );

            // assert
            assertThat(result.getCustomMessage()).isEqualTo("카드타입은 비어있을 수 없습니다.");
        }

        @DisplayName("cardNo 가 null 이면, CoreException 이 발생한다.")
        @Test
        void throwsCoreException_whenCardNoIsNull() {
            // arrange & act
            CoreException result = assertThrows(CoreException.class, () ->
                Payment.of("20260318-ABCD12", 1L, "신한카드", null, 50000L)
            );

            // assert
            assertThat(result.getCustomMessage()).isEqualTo("카드번호는 비어있을 수 없습니다.");
        }

        @DisplayName("cardNo 가 blank 이면, CoreException 이 발생한다.")
        @Test
        void throwsCoreException_whenCardNoIsBlank() {
            // arrange & act
            CoreException result = assertThrows(CoreException.class, () ->
                Payment.of("20260318-ABCD12", 1L, "신한카드", "  ", 50000L)
            );

            // assert
            assertThat(result.getCustomMessage()).isEqualTo("카드번호는 비어있을 수 없습니다.");
        }

        @DisplayName("amount 가 null 이면, CoreException 이 발생한다.")
        @Test
        void throwsCoreException_whenAmountIsNull() {
            // arrange & act
            CoreException result = assertThrows(CoreException.class, () ->
                Payment.of("20260318-ABCD12", 1L, "신한카드", "1234-5678-9012-3456", null)
            );

            // assert
            assertThat(result.getCustomMessage()).isEqualTo("결제금액은 양의 정수여야 합니다.");
        }

        @DisplayName("amount 가 0 이면, CoreException 이 발생한다.")
        @Test
        void throwsCoreException_whenAmountIsZero() {
            // arrange & act
            CoreException result = assertThrows(CoreException.class, () ->
                Payment.of("20260318-ABCD12", 1L, "신한카드", "1234-5678-9012-3456", 0L)
            );

            // assert
            assertThat(result.getCustomMessage()).isEqualTo("결제금액은 양의 정수여야 합니다.");
        }

        @DisplayName("amount 가 음수이면, CoreException 이 발생한다.")
        @Test
        void throwsCoreException_whenAmountIsNegative() {
            // arrange & act
            CoreException result = assertThrows(CoreException.class, () ->
                Payment.of("20260318-ABCD12", 1L, "신한카드", "1234-5678-9012-3456", -1L)
            );

            // assert
            assertThat(result.getCustomMessage()).isEqualTo("결제금액은 양의 정수여야 합니다.");
        }
    }
}
