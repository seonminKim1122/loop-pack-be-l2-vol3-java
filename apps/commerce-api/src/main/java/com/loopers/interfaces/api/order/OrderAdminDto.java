package com.loopers.interfaces.api.order;

import com.loopers.application.order.OrderAdminDetail;
import com.loopers.application.order.OrderAdminSummary;

import java.time.ZonedDateTime;
import java.util.List;

public class OrderAdminDto {

    public static record DetailResponse(
            Long orderId,
            Long totalPrice,
            List<ItemResponse> items,
            Long userId,
            String userName
    ) {
        public record ItemResponse(String productName, String brandName, Integer unitPrice, Integer quantity) {
        }

        public static DetailResponse from(OrderAdminDetail detail) {
            List<ItemResponse> itemResponses = detail.items().stream()
                    .map(item -> new ItemResponse(item.productName(), item.brandName(), item.unitPrice(), item.quantity()))
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
