package com.loopers.application.payment;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@RequiredArgsConstructor
@Component
public class PaymentFacade {

    private final PaymentApp paymentApp;

    public void processPayment(String orderId, String cardType, String cardNo, Long userId) {
        // 내부 결제 데이터 생성
        PaymentInfo paymentResult = paymentApp.pay(orderId, cardType, cardNo, userId);

        // 외부 PG사 연동

        // 연동 결과 반영
    }
}
