package com.loopers.application.payment;

import com.loopers.domain.payment.Payment;

public record PaymentInfo(
    String orderId,
    String cardType,
    String cardNo,
    Long amount
) {
    public static PaymentInfo from(Payment payment) {
        return new PaymentInfo(
                payment.orderId(),
                payment.cardType(),
                payment.cardNo(),
                payment.amount());
    }
}
