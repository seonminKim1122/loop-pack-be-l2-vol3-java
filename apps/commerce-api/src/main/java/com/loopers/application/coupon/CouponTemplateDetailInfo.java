package com.loopers.application.coupon;

import com.loopers.domain.coupon.CouponTemplate;

import java.time.ZonedDateTime;

public record CouponTemplateDetailInfo(Long id, String name, String type, int value, ZonedDateTime expiredAt) {

    public static CouponTemplateDetailInfo from(CouponTemplate couponTemplate) {
        return new CouponTemplateDetailInfo(couponTemplate.getId(),
                                            couponTemplate.name(),
                                            couponTemplate.couponType().name(),
                                            couponTemplate.value(),
                                            couponTemplate.expiredAt());
    }
}
