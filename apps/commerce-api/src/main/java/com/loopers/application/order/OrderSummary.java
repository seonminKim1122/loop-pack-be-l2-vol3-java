package com.loopers.application.order;

import com.loopers.domain.order.Order;

import java.time.ZonedDateTime;

public record OrderSummary(Long orderId, ZonedDateTime orderedAt, Long totalPrice, Integer itemCount) {

    public static OrderSummary from(Order order) {
        return new OrderSummary(order.getId(), order.getCreatedAt(), order.paymentAmount(), order.itemCount());
    }
}
