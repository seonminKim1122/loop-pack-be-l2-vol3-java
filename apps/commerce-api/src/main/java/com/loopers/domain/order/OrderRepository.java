package com.loopers.domain.order;

import java.time.ZonedDateTime;
import java.util.List;
import java.util.Optional;

public interface OrderRepository {

    Long save(Order order);

    Optional<Order> findById(Long orderId);

    List<Order> findAllByUserIdAndCreatedAtBetween(Long userId, ZonedDateTime startAt, ZonedDateTime endAt);
}
