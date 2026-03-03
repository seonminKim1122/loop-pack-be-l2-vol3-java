package com.loopers.application.coupon;

import com.loopers.domain.coupon.Coupon;

public record CouponListInfo(Long id, String name, String type) {

    public static CouponListInfo from(Coupon coupon) {
        return new CouponListInfo(coupon.getId(), coupon.name(), coupon.couponType().name());
    }
}
