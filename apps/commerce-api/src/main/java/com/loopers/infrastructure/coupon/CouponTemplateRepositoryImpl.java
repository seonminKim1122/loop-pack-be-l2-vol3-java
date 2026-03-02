package com.loopers.infrastructure.coupon;

import com.loopers.domain.coupon.CouponTemplate;
import com.loopers.domain.coupon.CouponTemplateRepository;
import com.loopers.support.page.PageResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Component;

import java.util.Optional;

@RequiredArgsConstructor
@Component
public class CouponTemplateRepositoryImpl implements CouponTemplateRepository {

    private final CouponTemplateJpaRepository jpaRepository;

    @Override
    public Long save(CouponTemplate couponTemplate) {
        CouponTemplate savedCouponTemplate = jpaRepository.save(couponTemplate);
        return savedCouponTemplate.getId();
    }

    @Override
    public Optional<CouponTemplate> findById(Long id) {
        return jpaRepository.findById(id);
    }

    @Override
    public void deleteById(Long id) {
        jpaRepository.deleteById(id);
    }

    @Override
    public PageResponse<CouponTemplate> findAll(Pageable pageable) {
        return PageResponse.from(jpaRepository.findAll(pageable));
    }
}
