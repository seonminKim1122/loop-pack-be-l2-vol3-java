package com.loopers.domain.payment;

import com.loopers.domain.BaseEntity;
import com.loopers.support.error.CoreException;
import com.loopers.support.error.ErrorType;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;

@Table(name = "payment")
@Entity
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Payment extends BaseEntity {

    @Column(name = "order_id", nullable = false, unique = true)
    private String orderId;

    @Column(name = "user_id", nullable = false)
    private Long userId;

    @Column(name = "card_type", nullable = false)
    private String cardType;

    @Column(name = "card_no", nullable = false)
    private String cardNo;

    @Column(name = "amount", nullable = false)
    private Long amount;

    @Column(name = "status")
    @Enumerated(EnumType.STRING)
    private PaymentStatus status;

    private Payment(String orderId, Long userId, String cardType, String cardNo, Long amount) {
        this.orderId = orderId;
        this.userId = userId;
        this.cardType = cardType;
        this.cardNo = cardNo;
        this.amount = amount;
        this.status = PaymentStatus.PENDING;
    }

    public static Payment of (String orderId, Long userId, String cardType, String cardNo, Long amount) {
        if (orderId == null || orderId.isBlank()) {
            throw new CoreException(ErrorType.BAD_REQUEST, "주문번호는 비어있을 수 없습니다.");
        }

        if (userId == null) {
            throw new CoreException(ErrorType.BAD_REQUEST, "결제자ID는 비어있을 수 없습니다.");
        }

        if (cardType == null || cardType.isBlank()) {
            throw new CoreException(ErrorType.BAD_REQUEST, "카드타입은 비어있을 수 없습니다.");
        }

        if (cardNo == null || cardNo.isBlank()) {
            throw new CoreException(ErrorType.BAD_REQUEST, "카드번호는 비어있을 수 없습니다.");
        }

        if (amount == null || amount <= 0) {
            throw new CoreException(ErrorType.BAD_REQUEST, "결제금액은 양의 정수여야 합니다.");
        }

        return new Payment(orderId, userId, cardType, cardNo, amount);
    }

    public String orderId() {
        return orderId;
    }

    public Long userId() {
        return userId;
    }

    public String cardType() {
        return cardType;
    }

    public String cardNo() {
        return cardNo;
    }

    public Long amount() {
        return amount;
    }

    public PaymentStatus status() {
        return status;
    }
}
