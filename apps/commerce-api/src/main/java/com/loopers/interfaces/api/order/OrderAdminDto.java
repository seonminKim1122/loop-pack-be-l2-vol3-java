package com.loopers.interfaces.api.order;

import com.loopers.application.order.OrderAdminDetail;
import com.loopers.application.order.OrderAdminSummary;
import com.loopers.application.order.OrderDetail;

import java.time.ZonedDateTime;
import java.util.List;

public class OrderAdminDto {

    public static record DetailResponse(
            Long orderId,
            Long totalPrice,
            List<OrderDto.ItemResponse> items,
            Long userId,
            String userName
    ) {
        public static DetailResponse from(OrderAdminDetail detail) {
            List<OrderDto.ItemResponse> itemResponses = detail.items().stream()
                    .map(item -> new OrderDto.ItemResponse(item.productName(), item.brandName(), item.unitPrice(), item.quantity()))
                    .toList();
            return new DetailResponse(detail.orderId(), detail.totalPrice(), itemResponses, detail.userId(), detail.userName());
        }
    }

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
