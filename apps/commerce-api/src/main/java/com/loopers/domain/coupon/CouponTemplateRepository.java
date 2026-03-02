package com.loopers.domain.coupon;

import com.loopers.support.page.PageResponse;
import org.springframework.data.domain.Pageable;

import java.util.Optional;

public interface CouponTemplateRepository {

    Long save(CouponTemplate couponTemplate);

    Optional<CouponTemplate> findById(Long id);

    void deleteById(Long id);

    PageResponse<CouponTemplate> findAll(Pageable pageable);
}
