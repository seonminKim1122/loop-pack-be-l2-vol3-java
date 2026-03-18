package com.loopers.domain.payment;

import java.util.Optional;

public interface PaymentRepository {

    void save(Payment payment);

    Optional<Payment> findByOrderId(String orderId);
}
