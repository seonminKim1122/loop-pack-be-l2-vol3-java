package com.loopers.application.coupon;

import com.loopers.domain.coupon.UserCoupon;

public record MyCouponInfo(Long id, String name, String type, int value, String status) {

    public static MyCouponInfo from(UserCoupon userCoupon) {
        return new MyCouponInfo(userCoupon.getId(), userCoupon.name(), userCoupon.couponType().name(), userCoupon.value(), userCoupon.status().name());
    }
}
