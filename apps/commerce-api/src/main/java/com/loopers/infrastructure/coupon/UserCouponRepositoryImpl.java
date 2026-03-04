package com.loopers.infrastructure.coupon;

import com.loopers.domain.coupon.UserCoupon;
import com.loopers.domain.coupon.UserCouponRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;

@RequiredArgsConstructor
@Component
public class UserCouponRepositoryImpl implements UserCouponRepository {

    private final UserCouponJpaRepository jpaRepository;

    @Override
    public Long save(UserCoupon userCoupon) {
        return jpaRepository.saveAndFlush(userCoupon).getId();
    }

    @Override
    public Optional<UserCoupon> findById(Long userCouponId) {
        return jpaRepository.findById(userCouponId);
    }

    @Override
    public List<UserCoupon> findAllByUserId(Long userId) {
        return jpaRepository.findAllByUserId(userId);
    }
}
