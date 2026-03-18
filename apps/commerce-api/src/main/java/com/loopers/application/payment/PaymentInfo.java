package com.loopers.application.payment;

import com.loopers.domain.payment.Payment;

public record PaymentInfo(
    Long paymentId,
    String orderId,
    String cardType,
    String cardNo,
    Long amount
) {
    public static PaymentInfo from(Payment payment) {
        return new PaymentInfo(
                payment.getId(),
                payment.orderId(),
                payment.cardType(),
                payment.cardNo(),
                payment.amount());
    }
}
