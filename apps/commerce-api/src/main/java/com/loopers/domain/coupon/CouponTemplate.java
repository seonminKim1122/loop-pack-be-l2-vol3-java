package com.loopers.domain.coupon;

import com.loopers.domain.BaseEntity;
import com.loopers.support.error.CoreException;
import com.loopers.support.error.ErrorType;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;

import java.time.ZonedDateTime;

@Entity
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class CouponTemplate extends BaseEntity {

    private String name;

    @Enumerated(EnumType.STRING)
    private CouponType couponType;

    private int value;

    private ZonedDateTime expiredAt;

    private CouponTemplate(String name, CouponType couponType, int value, ZonedDateTime expiredAt) {
        this.name = name;
        this.couponType = couponType;
        this.value = value;
        this.expiredAt = expiredAt;
    }

    public static CouponTemplate of(String name, String type, int value, ZonedDateTime expiredAt) {
        if (!CouponType.isValid(type)) {
            throw new CoreException(ErrorType.BAD_REQUEST, "유효하지 않은 쿠폰 타입입니다.");
        }
        CouponType couponType = CouponType.valueOf(type);
        if (value <= 0) {
            throw new CoreException(ErrorType.BAD_REQUEST, "할인 값은 0보다 커야 합니다.");
        }

        if (couponType == CouponType.RATE && value > 100) {
            throw new CoreException(ErrorType.BAD_REQUEST, "정률 할인일 때 할일율이 100퍼센트를 초과할 수 없습니다.");
        }

        return new CouponTemplate(name, couponType, value, expiredAt);
    }
}
