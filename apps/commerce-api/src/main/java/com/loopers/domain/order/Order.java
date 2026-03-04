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
@Table(name = "orders")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Order extends BaseEntity {

    @Column(name = "user_id")
    private Long userId;

    @Column(name = "issued_coupon_id")
    private Long issuedCouponId;

    @Column(name = "original_amount")
    private Long originalAmount;

    @Column(name = "discount_amount")
    private Long discountAmount;

    @Column(name = "payment_amount")
    private Long paymentAmount;

    @OneToMany(cascade = CascadeType.ALL, orphanRemoval = true)
    @JoinColumn(name = "order_id")
    private List<OrderItem> items;

    private Order(Long userId, Long issuedCouponId, long originalAmount, long discountAmount, List<OrderItem> items) {
        this.userId = userId;
        this.issuedCouponId = issuedCouponId;
        this.originalAmount = originalAmount;
        this.discountAmount = discountAmount;
        this.paymentAmount = Math.max(0L, originalAmount - discountAmount);
        this.items = items;
    }

    public static Order of(Long userId, List<OrderItem> items) {
        return of(userId, items, null, 0L);
    }

    public static Order of(Long userId, List<OrderItem> items, Long issuedCouponId, long discountAmount) {
        if (userId == null) {
            throw new CoreException(ErrorType.BAD_REQUEST, "사용자 ID는 필수입니다.");
        }
        if (items == null) {
            throw new CoreException(ErrorType.BAD_REQUEST, "주문 항목은 필수입니다.");
        }
        if (items.isEmpty()) {
            throw new CoreException(ErrorType.BAD_REQUEST, "주문 항목이 비어있습니다.");
        }

        long originalAmount = items.stream()
                .mapToLong(item -> (long) item.unitPrice() * item.quantity())
                .sum();

        return new Order(userId, issuedCouponId, originalAmount, discountAmount, items);
    }

    public Long originalAmount() {
        return originalAmount;
    }

    public Long discountAmount() {
        return discountAmount;
    }

    public Long paymentAmount() {
        return paymentAmount;
    }

    public Long issuedCouponId() {
        return issuedCouponId;
    }

    public int itemCount() {
        return items.size();
    }

    public List<OrderItem> items() {
        return List.copyOf(items);
    }

    public Long userId() {
        return userId;
    }

    public boolean isOwnedBy(Long userId) {
        return this.userId.equals(userId);
    }
}
