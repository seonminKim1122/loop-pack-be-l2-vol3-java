package com.loopers.domain.coupon;

import com.loopers.domain.BaseEntity;
import com.loopers.support.error.CoreException;
import com.loopers.support.error.ErrorType;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;

import java.time.ZonedDateTime;

@Entity
@Table(name ="ISSUED_COUPON")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class IssuedCoupon extends BaseEntity {

    @Column(name = "user_id")
    private Long userId;

    @Column(name = "coupon_template_id")
    private Long couponTemplateId;

    @Column(name = "name")
    private String name;

    @Column(name = "coupon_type")
    @Enumerated(EnumType.STRING)
    private CouponType couponType;

    @Column(name = "value")
    private int value;

    @Column(name = "expired_at")
    private ZonedDateTime expiredAt;

    @Column(name = "status")
    @Enumerated(EnumType.STRING)
    private CouponStatus status;

    private IssuedCoupon(Long userId, Long couponTemplateId, String name, CouponType couponType, int value, ZonedDateTime expiredAt) {
        this.userId = userId;
        this.couponTemplateId = couponTemplateId;
        this.name = name;
        this.couponType = couponType;
        this.value = value;
        this.expiredAt = expiredAt;
        this.status = CouponStatus.AVAILABLE;
    }

    public static IssuedCoupon of(CouponTemplate couponTemplate, Long userId) {
        if (couponTemplate == null) {
            throw new CoreException(ErrorType.BAD_REQUEST, "쿠폰 템플릿은 필수입니다.");
        }

        if (userId == null) {
            throw new CoreException(ErrorType.BAD_REQUEST, "사용자ID는 필수입니다.");
        }

        if (couponTemplate.isExpired()) {
            throw new CoreException(ErrorType.BAD_REQUEST, "만료된 쿠폰 템플릿입니다.");
        }

        return new IssuedCoupon(userId,
                couponTemplate.getId(),
                couponTemplate.name(),
                couponTemplate.couponType(),
                couponTemplate.value(),
                couponTemplate.expiredAt());
    }

    public String name() {
        return name;
    }

    public CouponType couponType() {
        return couponType;
    }

    public int value() {
        return value;
    }

    public CouponStatus status() {
        if (status == CouponStatus.AVAILABLE && expiredAt.isBefore(ZonedDateTime.now())) {
            return CouponStatus.EXPIRED;
        }

        return status;
    }

    public long calculateDiscount(long amount) {
        return couponType.calculate(amount, value);
    }
}
