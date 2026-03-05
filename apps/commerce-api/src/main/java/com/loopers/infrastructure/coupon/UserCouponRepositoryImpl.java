package com.loopers.infrastructure.coupon;

import com.loopers.domain.coupon.UserCoupon;
import com.loopers.domain.coupon.UserCouponRepository;
import com.loopers.support.error.CoreException;
import com.loopers.support.error.ErrorType;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.OptimisticLockingFailureException;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;

@RequiredArgsConstructor
@Component
public class UserCouponRepositoryImpl implements UserCouponRepository {

    private final UserCouponJpaRepository jpaRepository;

    @Override
    public Long save(UserCoupon userCoupon) {
        try {
            return jpaRepository.saveAndFlush(userCoupon).getId();
        } catch (OptimisticLockingFailureException e) {
            throw new CoreException(ErrorType.BAD_REQUEST, "이미 사용된 쿠폰입니다.");
        }
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
