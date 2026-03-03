package com.loopers.domain.coupon;

import java.util.List;

public interface UserCouponRepository {

    Long save(UserCoupon userCoupon);

    List<UserCoupon> findAllByUserId(Long userId);
}
