package com.loopers.domain.order;

import com.loopers.domain.BaseEntity;
import com.loopers.support.error.CoreException;
import com.loopers.support.error.ErrorType;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;

import java.util.List;

@Entity
@Table(name = "ORDERS")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Order extends BaseEntity {

    @Column(name = "user_id")
    private Long userId;

    @Column(name = "total_price")
    private Integer totalPrice;

    @OneToMany(cascade = CascadeType.ALL, orphanRemoval = true)
    @JoinColumn(name = "order_id")
    private List<OrderItem> items;

    private Order(Long userId, Integer totalPrice, List<OrderItem> items) {
        this.userId = userId;
        this.totalPrice = totalPrice;
        this.items = items;
    }

    public static Order of(Long userId, List<OrderItem> items) {
        if (userId == null) {
            throw new CoreException(ErrorType.BAD_REQUEST, "사용자 ID는 필수입니다.");
        }

        int totalPrice = items.stream()
                .mapToInt(item -> item.unitPrice() * item.quantity())
                .sum();

        return new Order(userId, totalPrice, items);
    }

    public Integer totalPrice() {
        return totalPrice;
    }
}
