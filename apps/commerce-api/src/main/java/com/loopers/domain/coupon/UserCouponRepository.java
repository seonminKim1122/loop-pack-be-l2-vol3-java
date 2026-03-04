package com.loopers.domain.coupon;

import java.util.List;
import java.util.Optional;

public interface UserCouponRepository {

    Long save(UserCoupon userCoupon);

    Optional<UserCoupon> findById(Long userCouponId);

    List<UserCoupon> findAllByUserId(Long userId);
}
