package com.loopers.application.coupon;

import com.loopers.domain.coupon.CouponTemplate;
import com.loopers.domain.coupon.CouponTemplateRepository;
import com.loopers.domain.coupon.IssuedCoupon;
import com.loopers.domain.coupon.IssuedCouponRepository;
import com.loopers.domain.user.User;
import com.loopers.domain.user.UserRepository;
import com.loopers.support.error.CoreException;
import com.loopers.support.error.ErrorType;
import com.loopers.support.page.PageResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.ZonedDateTime;
import java.util.List;

@RequiredArgsConstructor
@Component
public class CouponFacade {

    private final UserRepository userRepository;
    private final CouponTemplateRepository couponTemplateRepository;
    private final IssuedCouponRepository issuedCouponRepository;

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

    @Transactional
    public void delete(Long couponId) {
        couponTemplateRepository.deleteById(couponId);
    }

    @Transactional(readOnly = true)
    public PageResponse<CouponTemplateListInfo> getList(Pageable pageable) {
        return couponTemplateRepository.findAll(pageable).map(CouponTemplateListInfo::from);
    }

    @Transactional(readOnly = true)
    public CouponTemplateDetailInfo getDetail(Long couponId) {
        CouponTemplate couponTemplate = couponTemplateRepository.findById(couponId)
                .orElseThrow(() -> new CoreException(ErrorType.NOT_FOUND, "존재하지 않는 쿠폰 템플릿입니다."));

        return CouponTemplateDetailInfo.from(couponTemplate);
    }

    @Transactional
    public Long issue(Long couponId, Long userId) {

        CouponTemplate couponTemplate = couponTemplateRepository.findById(couponId)
                .orElseThrow(() -> new CoreException(ErrorType.NOT_FOUND, "존재하지 않는 쿠폰 템플릿입니다."));

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new CoreException(ErrorType.NOT_FOUND, "존재하지 않는 사용자입니다."));

        IssuedCoupon issuedCoupon = IssuedCoupon.of(couponTemplate, user.getId());
        return issuedCouponRepository.save(issuedCoupon);
    }

    @Transactional(readOnly = true)
    public List<MyCouponInfo> getMyCouponList(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new CoreException(ErrorType.NOT_FOUND, "존재하지 않는 사용자입니다."));

        List<IssuedCoupon> myCoupons = issuedCouponRepository.findAllByUserId(user.getId());
        return myCoupons.stream().map(MyCouponInfo::from).toList();
    }
}
