package com.loopers.infrastructure.payment.pg;

import com.loopers.application.payment.pg.PgClient;
import com.loopers.application.payment.pg.PgPaymentDto;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

@Component
public class PgClientImpl implements PgClient {

    private static final String PAYMENT_PATH = "/api/v1/payments";

    private final RestTemplate restTemplate;
    private final String baseUrl;
    private final String merchantId;

    public PgClientImpl(
            RestTemplate restTemplate,
            @Value("${payment.pg.base-url}") String baseUrl,
            @Value("${payment.pg.merchant-id}") String merchantId
    ) {
        this.restTemplate = restTemplate;
        this.baseUrl = baseUrl;
        this.merchantId = merchantId;
    }

    @Override
    public PgPaymentDto.TransactionResponse requestPayment(PgPaymentDto.PaymentRequest request) {
        HttpHeaders headers = new HttpHeaders();
        headers.set("X-USER-ID", merchantId);
        HttpEntity<PgPaymentDto.PaymentRequest> entity = new HttpEntity<>(request, headers);
        return restTemplate.postForObject(baseUrl + PAYMENT_PATH, entity, PgPaymentDto.TransactionResponse.class);
    }
}
