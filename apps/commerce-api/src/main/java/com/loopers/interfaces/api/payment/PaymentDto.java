package com.loopers.interfaces.api.payment;

public class PaymentDto {

    public static record PayRequest(
            String orderId,
            String cardType,
            String cardNo
    ){}
}
