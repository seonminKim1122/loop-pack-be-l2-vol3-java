package com.loopers.application.payment.pg;

public interface PgClient {

    PgPaymentDto.TransactionResponse requestPayment(PgPaymentDto.PaymentRequest request);
}
