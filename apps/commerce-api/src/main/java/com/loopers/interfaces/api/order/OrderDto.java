package com.loopers.interfaces.api.order;

import com.loopers.application.order.OrderSummary;

import java.time.ZonedDateTime;
import java.util.List;

public class OrderDto {

    public static record CreateOrderRequest(List<OrderItem> items) {
        public static record OrderItem(Long productId, Integer quantity) {

        }
    }

    public static record CreateOrderResponse(Long orderId) {

    }

    public static record SummaryResponse(Long orderId, ZonedDateTime orderedAt, Long totalPrice, Integer itemCount) {

        public static SummaryResponse from(OrderSummary orderSummary) {
            return new SummaryResponse(orderSummary.orderId(), orderSummary.orderedAt(), orderSummary.totalPrice(), orderSummary.itemCount());
        }
    }
}
