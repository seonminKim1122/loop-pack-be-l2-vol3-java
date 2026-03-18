package com.loopers.application.payment;

import com.loopers.domain.order.Order;
import com.loopers.domain.order.OrderRepository;
import com.loopers.domain.payment.PaymentRepository;
import com.loopers.support.error.CoreException;
import com.loopers.support.error.ErrorType;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class PaymentAppTest {

    OrderRepository orderRepository = mock(OrderRepository.class);
    PaymentRepository paymentRepository = mock(PaymentRepository.class);

    PaymentApp paymentApp = new PaymentApp(orderRepository, paymentRepository);

    @DisplayName("결제 요청 시, ")
    @Nested
    class Pay {

        @DisplayName("유효한 주문이고 본인 주문이면, Payment 가 저장되고 PaymentInfo 를 반환한다.")
        @Test
        void returnsPaymentInfo_andSavesPayment_whenValidOrder() {
            // arrange
            String orderId = "20260318-ABCD12";
            Long userId = 1L;
            String cardType = "신한카드";
            String cardNo = "1234-5678-9012-3456";

            Order order = mock(Order.class);
            when(order.userId()).thenReturn(userId);
            when(order.paymentAmount()).thenReturn(50000L);
            when(orderRepository.findByOrderId(orderId)).thenReturn(Optional.of(order));

            // act
            PaymentInfo result = paymentApp.pay(orderId, cardType, cardNo, userId);

            // assert
            assertThat(result).isNotNull();
            assertThat(result.orderId()).isEqualTo(orderId);
            verify(paymentRepository).save(any());
        }

        @DisplayName("존재하지 않는 주문번호이면, CoreException 이 발생한다.")
        @Test
        void throwsCoreException_whenOrderNotFound() {
            // arrange
            String orderId = "20260318-NOTFOUND";
            when(orderRepository.findByOrderId(orderId)).thenReturn(Optional.empty());

            // act
            CoreException result = assertThrows(CoreException.class, () ->
                paymentApp.pay(orderId, "신한카드", "1234-5678-9012-3456", 1L)
            );

            // assert
            assertThat(result.getErrorType()).isEqualTo(ErrorType.NOT_FOUND);
            assertThat(result.getCustomMessage()).isEqualTo("찾을 수 없는 주문번호입니다.");
        }

        @DisplayName("본인 주문이 아니면, CoreException 이 발생한다.")
        @Test
        void throwsCoreException_whenNotOrderOwner() {
            // arrange
            String orderId = "20260318-ABCD12";
            Long userId = 1L;
            Long anotherUserId = 2L;

            Order order = mock(Order.class);
            when(order.userId()).thenReturn(anotherUserId);
            when(orderRepository.findByOrderId(orderId)).thenReturn(Optional.of(order));

            // act
            CoreException result = assertThrows(CoreException.class, () ->
                paymentApp.pay(orderId, "신한카드", "1234-5678-9012-3456", userId)
            );

            // assert
            assertThat(result.getErrorType()).isEqualTo(ErrorType.FORBIDDEN);
            assertThat(result.getCustomMessage()).isEqualTo("본인의 주문에 대해서만 결제 가능합니다.");
        }
    }
}
