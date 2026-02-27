package com.loopers.application.order;

import com.loopers.domain.order.Order;

import java.util.List;

public record OrderDetail(Long orderId, Long totalPrice, List<OrderItemDetail> items) {

    public record OrderItemDetail(String productName, String brandName, Integer unitPrice, Integer quantity) {
    }

    public static OrderDetail from(Order order) {
        List<OrderItemDetail> itemDetails = order.items().stream()
                .map(item -> new OrderItemDetail(item.productName(), item.brandName(), item.unitPrice(), item.quantity()))
                .toList();
        return new OrderDetail(order.getId(), order.totalPrice(), itemDetails);
    }
}
