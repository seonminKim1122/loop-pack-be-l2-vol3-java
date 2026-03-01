package com.loopers.domain.coupon;

import java.util.Optional;

public interface CouponTemplateRepository {

    Long save(CouponTemplate couponTemplate);

    Optional<CouponTemplate> findById(Long id);
}
