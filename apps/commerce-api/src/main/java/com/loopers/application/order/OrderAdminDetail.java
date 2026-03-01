package com.loopers.application.order;

import com.loopers.domain.order.Order;
import com.loopers.domain.user.User;

import java.util.List;

public record OrderAdminDetail(
        Long orderId,
        Long totalPrice,
        List<OrderItemDetail> items,
        Long userId,
        String userName
) {
    public record OrderItemDetail(String productName, String brandName, Integer unitPrice, Integer quantity) {
    }

    public static OrderAdminDetail of(Order order, User user) {
        List<OrderItemDetail> itemDetails = order.items().stream()
                .map(item -> new OrderItemDetail(item.productName(), item.brandName(), item.unitPrice(), item.quantity()))
                .toList();
        return new OrderAdminDetail(order.getId(), order.totalPrice(), itemDetails, user.getId(), user.name().value());
    }
}
