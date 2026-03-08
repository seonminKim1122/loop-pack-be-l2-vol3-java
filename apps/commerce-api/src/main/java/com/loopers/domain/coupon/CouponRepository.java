package com.loopers.domain.coupon;

import com.loopers.support.page.PageResponse;
import org.springframework.data.domain.Pageable;

import java.util.Optional;

public interface CouponRepository {

    Long save(Coupon coupon);

    Optional<Coupon> findById(Long id);

    void deleteById(Long id);

    PageResponse<Coupon> findAll(Pageable pageable);
}
