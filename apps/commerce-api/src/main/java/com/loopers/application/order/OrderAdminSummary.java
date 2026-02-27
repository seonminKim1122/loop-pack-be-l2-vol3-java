package com.loopers.application.order;

import com.loopers.domain.order.Order;
import com.loopers.domain.user.User;

import java.time.ZonedDateTime;

public record OrderAdminSummary(
        Long orderId,
        ZonedDateTime orderedAt,
        Long totalPrice,
        Integer itemCount,
        Long userId,
        String userName
) {
    public static OrderAdminSummary of(Order order, User user) {
        return new OrderAdminSummary(
                order.getId(),
                order.getCreatedAt(),
                order.totalPrice(),
                order.itemCount(),
                user.getId(),
                user.name().value()
        );
    }
}
