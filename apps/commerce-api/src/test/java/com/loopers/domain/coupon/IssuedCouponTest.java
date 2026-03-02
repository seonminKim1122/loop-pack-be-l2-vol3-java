package com.loopers.domain.coupon;

import com.loopers.support.error.CoreException;
import com.loopers.support.error.ErrorType;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import java.time.ZonedDateTime;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

class IssuedCouponTest {

    @DisplayName("IssuedCoupon 을 생성할 때, ")
    @Nested
    class Create {

        @DisplayName("유효한 쿠폰 템플릿과 사용자 ID 로 생성하면, IssuedCoupon 이 생성된다.")
        @Test
        void createsIssuedCoupon_whenValidTemplateAndUserId() {
            // arrange
            CouponTemplate template = CouponTemplate.of("3000원 할인 쿠폰", "FIXED", 3000, ZonedDateTime.now().plusDays(30));
            Long userId = 1L;

            // act
            IssuedCoupon result = IssuedCoupon.of(template, userId);

            // assert
            assertThat(result).isNotNull();
        }

        @DisplayName("쿠폰 템플릿이 null 이면, BAD_REQUEST 예외가 발생한다.")
        @Test
        void throwsBadRequestException_whenTemplateIsNull() {
            // arrange & act
            CoreException result = assertThrows(CoreException.class, () ->
                IssuedCoupon.of(null, 1L)
            );

            // assert
            assertThat(result.getErrorType()).isEqualTo(ErrorType.BAD_REQUEST);
            assertThat(result.getCustomMessage()).isEqualTo("쿠폰 템플릿은 필수입니다.");
        }

        @DisplayName("사용자 ID 가 null 이면, BAD_REQUEST 예외가 발생한다.")
        @Test
        void throwsBadRequestException_whenUserIdIsNull() {
            // arrange
            CouponTemplate template = CouponTemplate.of("3000원 할인 쿠폰", "FIXED", 3000, ZonedDateTime.now().plusDays(30));

            // act
            CoreException result = assertThrows(CoreException.class, () ->
                IssuedCoupon.of(template, null)
            );

            // assert
            assertThat(result.getErrorType()).isEqualTo(ErrorType.BAD_REQUEST);
            assertThat(result.getCustomMessage()).isEqualTo("사용자ID는 필수입니다.");
        }

        @DisplayName("만료된 쿠폰 템플릿으로 생성하면, BAD_REQUEST 예외가 발생한다.")
        @Test
        void throwsBadRequestException_whenTemplateIsExpired() {
            // arrange
            CouponTemplate template = CouponTemplate.of("3000원 할인 쿠폰", "FIXED", 3000, ZonedDateTime.now().minusDays(1));

            // act
            CoreException result = assertThrows(CoreException.class, () ->
                IssuedCoupon.of(template, 1L)
            );

            // assert
            assertThat(result.getErrorType()).isEqualTo(ErrorType.BAD_REQUEST);
            assertThat(result.getCustomMessage()).isEqualTo("만료된 쿠폰 템플릿입니다.");
        }
    }

    @DisplayName("status() 를 호출할 때, ")
    @Nested
    class Status {

        @DisplayName("만료일이 지나지 않은 쿠폰은 AVAILABLE 을 반환한다.")
        @Test
        void returnsAvailable_whenCouponIsNotExpired() {
            // arrange
            CouponTemplate template = CouponTemplate.of("3000원 할인 쿠폰", "FIXED", 3000, ZonedDateTime.now().plusDays(30));
            IssuedCoupon issuedCoupon = IssuedCoupon.of(template, 1L);

            // act
            CouponStatus result = issuedCoupon.status();

            // assert
            assertThat(result).isEqualTo(CouponStatus.AVAILABLE);
        }

        @DisplayName("만료일이 지난 쿠폰은 EXPIRED 를 반환한다.")
        @Test
        void returnsExpired_whenCouponIsExpired() throws Exception {
            // arrange
            CouponTemplate template = CouponTemplate.of("3000원 할인 쿠폰", "FIXED", 3000, ZonedDateTime.now().plusDays(30));
            IssuedCoupon issuedCoupon = IssuedCoupon.of(template, 1L);

            java.lang.reflect.Field field = IssuedCoupon.class.getDeclaredField("expiredAt");
            field.setAccessible(true);
            field.set(issuedCoupon, ZonedDateTime.now().minusDays(1));

            // act
            CouponStatus result = issuedCoupon.status();

            // assert
            assertThat(result).isEqualTo(CouponStatus.EXPIRED);
        }
    }
}
