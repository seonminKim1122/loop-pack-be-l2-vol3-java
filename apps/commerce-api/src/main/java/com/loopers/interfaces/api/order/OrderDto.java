package com.loopers.interfaces.api.order;

import java.util.List;

public class OrderDto {

    public static record CreateOrderRequest(List<OrderItem> items) {
        public static record OrderItem(Long productId, Integer quantity) {

        }
    }

    public static record CreateOrderResponse(Long orderId) {

    }
}
