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
    private Long totalPrice;

    @OneToMany(cascade = CascadeType.ALL, orphanRemoval = true)
    @JoinColumn(name = "order_id")
    private List<OrderItem> items;

    private Order(Long userId, Long totalPrice, List<OrderItem> items) {
        this.userId = userId;
        this.totalPrice = totalPrice;
        this.items = items;
    }

    public static Order of(Long userId, List<OrderItem> items) {
        if (userId == null) {
            throw new CoreException(ErrorType.BAD_REQUEST, "사용자 ID는 필수입니다.");
        }
        if (items == null) {
            throw new CoreException(ErrorType.BAD_REQUEST, "주문 항목은 필수입니다.");
        }
        if (items.isEmpty()) {
            throw new CoreException(ErrorType.BAD_REQUEST, "주문 항목이 비어있습니다.");
        }

        long totalPrice = items.stream()
                .mapToLong(item -> (long) item.unitPrice() * item.quantity())
                .sum();

        return new Order(userId, totalPrice, items);
    }

    public Long totalPrice() {
        return totalPrice;
    }

    public int itemCount() {
        return items.size();
    }

    public List<OrderItem> items() {
        return List.copyOf(items);
    }

    public boolean isOwnedBy(Long userId) {
        return this.userId.equals(userId);
    }
}
