package com.loopers.domain.order;

import java.time.ZonedDateTime;
import java.util.List;

public interface OrderRepository {

    Long save(Order order);

    List<Order> findAllByUserIdAndCreatedAtBetween(Long userId, ZonedDateTime startAt, ZonedDateTime endAt);
}
