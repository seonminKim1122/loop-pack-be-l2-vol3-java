package com.loopers.infrastructure.order;

import com.loopers.domain.order.Order;
import com.loopers.domain.order.OrderRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.time.ZonedDateTime;
import java.util.List;
import java.util.Optional;

@RequiredArgsConstructor
@Component
public class OrderRepositoryImpl implements OrderRepository {

    private final OrderJpaRepository jpaRepository;

    @Override
    public Long save(Order order) {
        return jpaRepository.save(order).getId();
    }

    @Override
    public Optional<Order> findById(Long orderId) {
        return jpaRepository.findById(orderId);
    }

    @Override
    public List<Order> findAllByUserIdAndCreatedAtBetween(Long userId, ZonedDateTime startAt, ZonedDateTime endAt) {
        return jpaRepository.findAllByUserIdAndCreatedAtBetween(userId, startAt, endAt);
    }
}
