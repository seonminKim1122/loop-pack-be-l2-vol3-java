package com.loopers.infrastructure.payment.pg;

import com.loopers.application.payment.pg.PgClient;
import com.loopers.application.payment.pg.PgPaymentDto;
import com.loopers.support.error.CoreException;
import com.loopers.support.error.ErrorType;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import io.github.resilience4j.retry.annotation.Retry;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.HttpServerErrorException;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.client.RestTemplate;

import java.net.SocketTimeoutException;
import java.util.Optional;

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

    @CircuitBreaker(name = "pg", fallbackMethod = "requestPaymentFallback")
    @Retry(name = "pg")
    @Override
    public PgPaymentDto.TransactionResponse requestPayment(PgPaymentDto.PaymentRequest request) {
        HttpHeaders headers = new HttpHeaders();
        headers.set("X-USER-ID", merchantId);
        HttpEntity<PgPaymentDto.PaymentRequest> entity = new HttpEntity<>(request, headers);

        try {
            ResponseEntity<PgApiResponse<PgPaymentDto.TransactionResponse>> response = restTemplate.exchange(
                    baseUrl + PAYMENT_PATH,
                    HttpMethod.POST,
                    entity,
                    new ParameterizedTypeReference<>() {}
            );
            return response.getBody().data();
        } catch (HttpClientErrorException e) {
            // 400: 재시도 불가 - CoreException 은 ignore-exceptions 로 설정
            throw new CoreException(ErrorType.BAD_REQUEST, "PG 결제 요청이 잘못되었습니다.");
        } catch (ResourceAccessException e) {
            if (e.getCause() instanceof SocketTimeoutException ste && ste.getMessage().contains("connect timed out")) {
                throw e; // 연결 타임아웃: PG 미수신 확실 → Resilience4j 가 retry
            }
            // 읽기 타임아웃: PG 수신 가능성 있음 → 중복 결제 위험, 재시도 불가
            throw new CoreException(ErrorType.BAD_GATEWAY, "PG 서버 응답 시간이 초과되었습니다.");
        }
        // HttpServerErrorException(5xx) → Resilience4j 가 최대 3회 retry
    }

    private PgPaymentDto.TransactionResponse requestPaymentFallback(PgPaymentDto.PaymentRequest request, Exception e) {
        return new PgPaymentDto.TransactionResponse(null, PgPaymentDto.TransactionStatus.FAILED, null);
    }

    private Optional<PgPaymentDto.TransactionResponse> getTransactionFallback(String transactionKey, Exception e) {
        return Optional.empty();
    }

    @CircuitBreaker(name = "pg", fallbackMethod = "getTransactionFallback")
    @Override
    public Optional<PgPaymentDto.TransactionResponse> getTransaction(String transactionKey) {
        HttpEntity<Void> entity = new HttpEntity<>(authHeaders());
        try {
            ResponseEntity<PgApiResponse<PgPaymentDto.TransactionResponse>> response = restTemplate.exchange(
                    baseUrl + PAYMENT_PATH + "/" + transactionKey,
                    HttpMethod.GET,
                    entity,
                    new ParameterizedTypeReference<>() {}
            );
            return Optional.ofNullable(response.getBody().data());
        } catch (HttpClientErrorException.NotFound e) {
            return Optional.empty();
        } catch (HttpClientErrorException e) {
            throw new CoreException(ErrorType.BAD_REQUEST, "PG 트랜잭션 조회 요청이 잘못되었습니다.");
        } catch (HttpServerErrorException e) {
            throw new CoreException(ErrorType.INTERNAL_ERROR, "PG 서버 오류가 발생했습니다.");
        } catch (ResourceAccessException e) {
            throw new CoreException(ErrorType.BAD_GATEWAY, "PG 서버 응답 시간이 초과되었습니다.");
        }
    }

    @CircuitBreaker(name = "pg")
    @Override
    public Optional<PgPaymentDto.TransactionListResponse> getTransactionsByOrderId(String orderId) {
        HttpEntity<Void> entity = new HttpEntity<>(authHeaders());
        try {
            ResponseEntity<PgApiResponse<PgPaymentDto.TransactionListResponse>> response = restTemplate.exchange(
                    baseUrl + PAYMENT_PATH + "?orderId=" + orderId,
                    HttpMethod.GET,
                    entity,
                    new ParameterizedTypeReference<>() {}
            );
            return Optional.ofNullable(response.getBody().data());
        } catch (HttpClientErrorException.NotFound e) {
            return Optional.empty();
        } catch (HttpClientErrorException e) {
            throw new CoreException(ErrorType.BAD_REQUEST, "PG 트랜잭션 목록 조회 요청이 잘못되었습니다.");
        } catch (HttpServerErrorException e) {
            throw new CoreException(ErrorType.INTERNAL_ERROR, "PG 서버 오류가 발생했습니다.");
        } catch (ResourceAccessException e) {
            throw new CoreException(ErrorType.BAD_GATEWAY, "PG 서버 응답 시간이 초과되었습니다.");
        }
    }

    private HttpHeaders authHeaders() {
        HttpHeaders headers = new HttpHeaders();
        headers.set("X-USER-ID", merchantId);
        return headers;
    }
}
