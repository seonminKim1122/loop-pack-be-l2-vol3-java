package com.loopers.application.coupon;

import com.loopers.domain.coupon.CouponTemplate;

public record CouponTemplateListInfo(Long id, String name, String type) {

    public static CouponTemplateListInfo from(CouponTemplate couponTemplate) {
        return new CouponTemplateListInfo(couponTemplate.getId(), couponTemplate.name(), couponTemplate.couponType().name());
    }
}
