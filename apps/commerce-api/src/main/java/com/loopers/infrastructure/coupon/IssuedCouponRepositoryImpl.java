package com.loopers.infrastructure.coupon;

import com.loopers.domain.coupon.IssuedCoupon;
import com.loopers.domain.coupon.IssuedCouponRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;

@RequiredArgsConstructor
@Component
public class IssuedCouponRepositoryImpl implements IssuedCouponRepository {

    private final IssuedCouponJpaRepository jpaRepository;

    @Override
    public Long save(IssuedCoupon issuedCoupon) {
        return jpaRepository.save(issuedCoupon).getId();
    }

    @Override
    public List<IssuedCoupon> findAllByUserId(Long userId) {
        return jpaRepository.findAllByUserId(userId);
    }
}
