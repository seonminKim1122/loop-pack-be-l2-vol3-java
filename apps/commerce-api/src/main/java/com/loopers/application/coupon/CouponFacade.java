package com.loopers.application.coupon;

import com.loopers.domain.coupon.CouponTemplate;
import com.loopers.domain.coupon.CouponTemplateRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.ZonedDateTime;

@RequiredArgsConstructor
@Component
public class CouponFacade {

    private final CouponTemplateRepository couponTemplateRepository;

    @Transactional
    public Long registerTemplate(String name, String type, int value, ZonedDateTime expiredAt) {

        CouponTemplate couponTemplate = CouponTemplate.of(name, type, value, expiredAt);
        return couponTemplateRepository.save(couponTemplate);
    }
}
