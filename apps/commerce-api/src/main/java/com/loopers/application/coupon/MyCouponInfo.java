package com.loopers.application.coupon;

import com.loopers.domain.coupon.IssuedCoupon;

public record MyCouponInfo(Long id, String name, String type, int value, String status) {

    public static MyCouponInfo from(IssuedCoupon issuedCoupon) {
        return new MyCouponInfo(issuedCoupon.getId(), issuedCoupon.name(), issuedCoupon.couponType().name(), issuedCoupon.value(), issuedCoupon.status().name());
    }
}
