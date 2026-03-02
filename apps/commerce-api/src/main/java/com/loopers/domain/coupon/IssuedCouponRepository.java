package com.loopers.domain.coupon;

import java.util.List;

public interface IssuedCouponRepository {

    Long save(IssuedCoupon issuedCoupon);

    List<IssuedCoupon> findAllByUserId(Long userId);
}
