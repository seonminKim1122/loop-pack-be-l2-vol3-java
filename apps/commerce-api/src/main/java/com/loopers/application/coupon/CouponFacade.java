package com.loopers.application.coupon;

import com.loopers.domain.coupon.CouponTemplate;
import com.loopers.domain.coupon.CouponTemplateRepository;
import com.loopers.support.error.CoreException;
import com.loopers.support.error.ErrorType;
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

    @Transactional
    public void updateTemplate(Long couponId, String name, int value, ZonedDateTime expiredAt) {
        CouponTemplate couponTemplate = couponTemplateRepository.findById(couponId)
                .orElseThrow(() -> new CoreException(ErrorType.NOT_FOUND, "존재하지 않는 쿠폰 템플릿입니다."));

        couponTemplate.update(name, value, expiredAt);
    }
}
