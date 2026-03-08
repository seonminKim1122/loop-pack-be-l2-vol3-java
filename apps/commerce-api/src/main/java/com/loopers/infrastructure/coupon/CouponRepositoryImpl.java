package com.loopers.infrastructure.coupon;

import com.loopers.domain.coupon.Coupon;
import com.loopers.domain.coupon.CouponRepository;
import com.loopers.support.page.PageResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Component;

import java.util.Optional;

@RequiredArgsConstructor
@Component
public class CouponRepositoryImpl implements CouponRepository {

    private final CouponJpaRepository jpaRepository;

    @Override
    public Long save(Coupon coupon) {
        return jpaRepository.save(coupon).getId();
    }

    @Override
    public Optional<Coupon> findById(Long id) {
        return jpaRepository.findById(id);
    }

    @Override
    public void deleteById(Long id) {
        jpaRepository.deleteById(id);
    }

    @Override
    public PageResponse<Coupon> findAll(Pageable pageable) {
        return PageResponse.from(jpaRepository.findAll(pageable));
    }
}
