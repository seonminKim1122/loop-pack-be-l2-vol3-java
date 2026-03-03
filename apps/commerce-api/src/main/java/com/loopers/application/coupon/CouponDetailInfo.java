package com.loopers.application.coupon;

import com.loopers.domain.coupon.Coupon;

import java.time.ZonedDateTime;

public record CouponDetailInfo(Long id, String name, String type, int value, ZonedDateTime expiredAt) {

    public static CouponDetailInfo from(Coupon coupon) {
        return new CouponDetailInfo(coupon.getId(),
                                    coupon.name(),
                                    coupon.couponType().name(),
                                    coupon.value(),
                                    coupon.expiredAt());
    }
}
