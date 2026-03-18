package com.loopers.application.payment;

import com.loopers.application.payment.pg.PgClient;
import com.loopers.application.payment.pg.PgPaymentDto;
import com.loopers.domain.payment.PaymentStatus;
import com.loopers.support.error.CoreException;
import com.loopers.support.error.ErrorType;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.ZonedDateTime;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.isNull;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class PaymentFacadeTest {

    PaymentApp paymentApp = mock(PaymentApp.class);
    PgClient pgClient = mock(PgClient.class);

    PaymentFacade paymentFacade = new PaymentFacade(paymentApp, pgClient);

    @BeforeEach
    void setUp() {
        ReflectionTestUtils.setField(paymentFacade, "callbackUrl", "http://localhost:8080/callback");
    }

    @DisplayName("결제 처리 시, ")
    @Nested
    class ProcessPayment {

        @DisplayName("PG 요청이 성공하면, PG 응답이 반영된다.")
        @Test
        void appliesPgResponse_whenPgRequestSucceeds() {
            // arrange
            String orderId = "20260318-ABCD12";
            PaymentInfo paymentInfo = new PaymentInfo(orderId, "신한카드", "1234-5678-9012-3456", 50000L, PaymentStatus.PENDING, null, null, ZonedDateTime.now());
            PgPaymentDto.TransactionResponse pgResponse = new PgPaymentDto.TransactionResponse(
                    "tx-key-001", PgPaymentDto.TransactionStatus.SUCCESS, null);

            when(paymentApp.pay(orderId, "신한카드", "1234-5678-9012-3456", 1L)).thenReturn(paymentInfo);
            when(pgClient.requestPayment(any())).thenReturn(pgResponse);

            // act
            paymentFacade.processPayment(orderId, "신한카드", "1234-5678-9012-3456", 1L);

            // assert
            verify(paymentApp).applyPgResponse(orderId, "tx-key-001", "SUCCESS", null);
        }

        @DisplayName("PG 요청에서 400 에러가 발생하면, Payment 를 FAILED 처리하고 예외를 재발생시킨다.")
        @Test
        void failsPaymentAndRethrows_whenPgReturns400() {
            // arrange
            String orderId = "20260318-ABCD12";
            PaymentInfo paymentInfo = new PaymentInfo(orderId, "신한카드", "1234-5678-9012-3456", 50000L, PaymentStatus.PENDING, null, null, ZonedDateTime.now());

            when(paymentApp.pay(orderId, "신한카드", "1234-5678-9012-3456", 1L)).thenReturn(paymentInfo);
            when(pgClient.requestPayment(any())).thenThrow(new CoreException(ErrorType.BAD_REQUEST, "PG 결제 요청이 잘못되었습니다."));

            // act
            CoreException result = assertThrows(CoreException.class, () ->
                paymentFacade.processPayment(orderId, "신한카드", "1234-5678-9012-3456", 1L)
            );

            // assert
            assertThat(result.getErrorType()).isEqualTo(ErrorType.BAD_REQUEST);
            verify(paymentApp).applyPgResponse(eq(orderId), isNull(), eq("FAILED"), any());
        }

        @DisplayName("PG 요청에서 500 에러가 발생하면, Payment 를 FAILED 처리하고 예외를 재발생시킨다.")
        @Test
        void failsPaymentAndRethrows_whenPgReturns500() {
            // arrange
            String orderId = "20260318-ABCD12";
            PaymentInfo paymentInfo = new PaymentInfo(orderId, "신한카드", "1234-5678-9012-3456", 50000L, PaymentStatus.PENDING, null, null, ZonedDateTime.now());

            when(paymentApp.pay(orderId, "신한카드", "1234-5678-9012-3456", 1L)).thenReturn(paymentInfo);
            when(pgClient.requestPayment(any())).thenThrow(new CoreException(ErrorType.INTERNAL_ERROR, "PG 서버 오류가 발생했습니다."));

            // act
            CoreException result = assertThrows(CoreException.class, () ->
                paymentFacade.processPayment(orderId, "신한카드", "1234-5678-9012-3456", 1L)
            );

            // assert
            assertThat(result.getErrorType()).isEqualTo(ErrorType.INTERNAL_ERROR);
            verify(paymentApp).applyPgResponse(eq(orderId), isNull(), eq("FAILED"), any());
        }
    }

    @DisplayName("결제 조회 시, ")
    @Nested
    class GetPayment {

        @DisplayName("SUCCESS 상태이면, PG 조회 없이 그대로 반환한다.")
        @Test
        void returnsAsIs_whenStatusIsSuccess() {
            // arrange
            String orderId = "20260318-ABCD12";
            Long userId = 1L;
            PaymentInfo paymentInfo = new PaymentInfo(orderId, "신한카드", "1234-5678-9012-3456", 50000L,
                    PaymentStatus.SUCCESS, "정상 승인되었습니다.", "tx-key-001", ZonedDateTime.now().minusSeconds(10));
            when(paymentApp.getPayment(orderId, userId)).thenReturn(paymentInfo);

            // act
            PaymentInfo result = paymentFacade.getPayment(orderId, userId);

            // assert
            assertThat(result.status()).isEqualTo(PaymentStatus.SUCCESS);
            verify(pgClient, never()).getTransaction(any());
        }

        @DisplayName("PENDING 상태이고 transactionKey 가 없으면, PG 조회 없이 그대로 반환한다.")
        @Test
        void returnsAsIs_whenPendingWithNoTransactionKey() {
            // arrange
            String orderId = "20260318-ABCD12";
            Long userId = 1L;
            PaymentInfo paymentInfo = new PaymentInfo(orderId, "신한카드", "1234-5678-9012-3456", 50000L,
                    PaymentStatus.PENDING, null, null, ZonedDateTime.now().minusSeconds(10));
            when(paymentApp.getPayment(orderId, userId)).thenReturn(paymentInfo);

            // act
            PaymentInfo result = paymentFacade.getPayment(orderId, userId);

            // assert
            assertThat(result.status()).isEqualTo(PaymentStatus.PENDING);
            verify(pgClient, never()).getTransaction(any());
        }

        @DisplayName("PENDING 상태이고 5초 이내이면, PG 조회 없이 그대로 반환한다.")
        @Test
        void returnsAsIs_whenPendingWithinFiveSeconds() {
            // arrange
            String orderId = "20260318-ABCD12";
            Long userId = 1L;
            PaymentInfo paymentInfo = new PaymentInfo(orderId, "신한카드", "1234-5678-9012-3456", 50000L,
                    PaymentStatus.PENDING, null, "tx-key-001", ZonedDateTime.now().minusSeconds(3));
            when(paymentApp.getPayment(orderId, userId)).thenReturn(paymentInfo);

            // act
            PaymentInfo result = paymentFacade.getPayment(orderId, userId);

            // assert
            assertThat(result.status()).isEqualTo(PaymentStatus.PENDING);
            verify(pgClient, never()).getTransaction(any());
        }

        @DisplayName("PENDING 상태이고 5초 초과이면, PG 조회 후 반영된 최신 상태를 반환한다.")
        @Test
        void queriesPgAndReturnsUpdated_whenPendingAfterFiveSeconds() {
            // arrange
            String orderId = "20260318-ABCD12";
            String transactionKey = "tx-key-001";
            Long userId = 1L;
            PaymentInfo pendingInfo = new PaymentInfo(orderId, "신한카드", "1234-5678-9012-3456", 50000L,
                    PaymentStatus.PENDING, null, transactionKey, ZonedDateTime.now().minusSeconds(10));
            PaymentInfo successInfo = new PaymentInfo(orderId, "신한카드", "1234-5678-9012-3456", 50000L,
                    PaymentStatus.SUCCESS, "정상 승인되었습니다.", transactionKey, ZonedDateTime.now().minusSeconds(10));

            when(paymentApp.getPayment(orderId, userId)).thenReturn(pendingInfo, successInfo);
            when(pgClient.getTransaction(transactionKey)).thenReturn(
                    Optional.of(new PgPaymentDto.TransactionResponse(transactionKey, PgPaymentDto.TransactionStatus.SUCCESS, "정상 승인되었습니다.")));

            // act
            PaymentInfo result = paymentFacade.getPayment(orderId, userId);

            // assert
            assertThat(result.status()).isEqualTo(PaymentStatus.SUCCESS);
            verify(paymentApp).applyPgResponse(orderId, transactionKey, "SUCCESS", "정상 승인되었습니다.");
        }

        @DisplayName("PENDING 상태이고 5초 초과이지만 PG 에서 404 이면, PENDING 그대로 반환한다.")
        @Test
        void returnsAsIs_whenPendingAfterFiveSecondsButPgReturns404() {
            // arrange
            String orderId = "20260318-ABCD12";
            String transactionKey = "tx-key-001";
            Long userId = 1L;
            PaymentInfo pendingInfo = new PaymentInfo(orderId, "신한카드", "1234-5678-9012-3456", 50000L,
                    PaymentStatus.PENDING, null, transactionKey, ZonedDateTime.now().minusSeconds(10));

            when(paymentApp.getPayment(orderId, userId)).thenReturn(pendingInfo);
            when(pgClient.getTransaction(transactionKey)).thenReturn(Optional.empty());

            // act
            PaymentInfo result = paymentFacade.getPayment(orderId, userId);

            // assert
            assertThat(result.status()).isEqualTo(PaymentStatus.PENDING);
            verify(paymentApp, never()).applyPgResponse(any(), any(), any(), any());
        }
    }
}
