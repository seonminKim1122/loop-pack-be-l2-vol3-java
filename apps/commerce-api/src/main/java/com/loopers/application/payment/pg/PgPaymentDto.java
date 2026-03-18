package com.loopers.application.payment.pg;

import com.loopers.application.payment.PaymentInfo;

public class PgPaymentDto {

    public static record PaymentRequest(
            String orderId,
            String cardType,
            String cardNo,
            Long amount,
            String callbackUrl
    ){
        public static PaymentRequest of(PaymentInfo paymentInfo, String callbackUrl) {
            return new PaymentRequest(
                    paymentInfo.orderId(),
                    paymentInfo.cardType(),
                    paymentInfo.cardNo(),
                    paymentInfo.amount(),
                    callbackUrl);
        }
    }

    public static record TransactionResponse(
            String transactionKey,
            TransactionStatus status,
            String reason
    ){}

    public enum TransactionStatus {
        PENDING,
        SUCCESS,
        FAILED
    }
}
