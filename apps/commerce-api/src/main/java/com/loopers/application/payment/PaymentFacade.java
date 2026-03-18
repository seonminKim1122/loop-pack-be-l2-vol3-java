package com.loopers.application.payment;

import com.loopers.application.payment.pg.PgClient;
import com.loopers.application.payment.pg.PgPaymentDto;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

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
        PgPaymentDto.PaymentRequest request = PgPaymentDto.PaymentRequest.of(
                paymentInfo,
                callbackUrl);
        PgPaymentDto.TransactionResponse response = pgClient.requestPayment(request);

        // PG사 응답 반영
        paymentApp.applyPgResponse(paymentInfo.paymentId(), response.transactionKey(), response.status().name(), response.reason());
    }
}
