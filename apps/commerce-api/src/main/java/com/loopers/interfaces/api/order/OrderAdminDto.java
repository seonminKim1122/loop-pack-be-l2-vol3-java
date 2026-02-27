package com.loopers.interfaces.api.order;

import com.loopers.application.order.OrderAdminSummary;

import java.time.ZonedDateTime;

public class OrderAdminDto {

    public static record SummaryResponse(
            Long orderId,
            ZonedDateTime orderedAt,
            Long totalPrice,
            Integer itemCount,
            Long userId,
            String userName
    ) {
        public static SummaryResponse from(OrderAdminSummary summary) {
            return new SummaryResponse(
                    summary.orderId(),
                    summary.orderedAt(),
                    summary.totalPrice(),
                    summary.itemCount(),
                    summary.userId(),
                    summary.userName()
            );
        }
    }
}
