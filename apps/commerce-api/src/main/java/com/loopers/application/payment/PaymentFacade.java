package com.loopers.application.payment;

import com.loopers.application.payment.pg.PgClient;
import com.loopers.application.payment.pg.PgPaymentDto;
import com.loopers.domain.payment.PaymentStatus;
import com.loopers.support.error.CoreException;
import com.loopers.support.error.ErrorType;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.time.ZonedDateTime;
import java.util.Optional;

@RequiredArgsConstructor
@Component
public class PaymentFacade {

    private final PaymentApp paymentApp;
    private final PgClient pgClient;

    @Value("${payment.pg.callback-url}")
    private String callbackUrl;

    public void processPayment(String orderId, String cardType, String cardNo, Long userId) {
        // 내부 결제 데이터 생성
        PaymentInfo paymentInfo = paymentApp.pay(orderId, cardType, cardNo, userId);

        // 외부 PG사 연동
        PgPaymentDto.PaymentRequest request = PgPaymentDto.PaymentRequest.of(paymentInfo, callbackUrl);
        try {
            PgPaymentDto.TransactionResponse response = pgClient.requestPayment(request);
            paymentApp.applyPgResponse(paymentInfo.orderId(), response.transactionKey(), response.status().name(), response.reason());

            if (response.status() == PgPaymentDto.TransactionStatus.FAILED) {
                // retry 소진 후 fallback — PG 미처리 확정, 클라이언트에 알림
                throw new CoreException(ErrorType.BAD_GATEWAY, "PG 서버 오류로 결제에 실패했습니다.");
            }
        } catch (CoreException e) {
            // 읽기 타임아웃(BAD_GATEWAY): 결제 상태를 PENDING 유지, 중복 결제 방지
            // 그 외(BAD_REQUEST 등): FAILED 저장
            if (e.getErrorType() != ErrorType.BAD_GATEWAY) {
                paymentApp.applyPgResponse(paymentInfo.orderId(), null, "FAILED", e.getCustomMessage());
            }
            throw e;
        }
    }

    public PaymentInfo getPayment(String orderId, Long userId) {
        PaymentInfo paymentInfo = paymentApp.getPayment(orderId, userId);

        if (paymentInfo.status() == PaymentStatus.PENDING
                && paymentInfo.transactionKey() != null
                && paymentInfo.createdAt().plusSeconds(5).isBefore(ZonedDateTime.now())) {

            try {
                Optional<PgPaymentDto.TransactionResponse> pgResponse = pgClient.getTransaction(paymentInfo.transactionKey());
                if (pgResponse.isPresent()) {
                    PgPaymentDto.TransactionResponse pg = pgResponse.get();
                    paymentApp.applyPgResponse(orderId, pg.transactionKey(), pg.status().name(), pg.reason());
                    return paymentApp.getPayment(orderId, userId);
                }
            } catch (CoreException ignored) {
                // PG 장애 시 현재 DB 상태 반환 — 조회는 best-effort, reconciliation이 동기화
            }
        }

        return paymentInfo;
    }

    public void handleCallback(String transactionKey, String orderId, String status, String reason) {
        paymentApp.applyPgResponse(orderId, transactionKey, status, reason);
    }
}
