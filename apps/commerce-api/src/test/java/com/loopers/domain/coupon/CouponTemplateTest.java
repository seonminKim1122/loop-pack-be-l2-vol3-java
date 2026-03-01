package com.loopers.domain.coupon;

import com.loopers.support.error.CoreException;
import com.loopers.support.error.ErrorType;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.time.ZonedDateTime;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

class CouponTemplateTest {

    @DisplayName("CouponTemplate 을 생성할 때, ")
    @Nested
    class Create {

        @DisplayName("정액 할인 쿠폰을 정상적인 값으로 생성하면, CouponTemplate 이 생성된다.")
        @Test
        void createsCouponTemplate_whenFixedTypeWithValidValue() {
            // arrange
            String name = "3000원 할인 쿠폰";
            CouponType couponType = CouponType.FIXED;
            int value = 3000;
            ZonedDateTime expiredAt = ZonedDateTime.now().plusDays(30);

            // act
            CouponTemplate result = CouponTemplate.of(name, couponType, value, expiredAt);

            // assert
            assertThat(result).isNotNull();
        }

        @DisplayName("정률 할인 쿠폰을 정상적인 값으로 생성하면, CouponTemplate 이 생성된다.")
        @Test
        void createsCouponTemplate_whenRateTypeWithValidValue() {
            // arrange
            String name = "10% 할인 쿠폰";
            CouponType couponType = CouponType.RATE;
            int value = 10;
            ZonedDateTime expiredAt = ZonedDateTime.now().plusDays(30);

            // act
            CouponTemplate result = CouponTemplate.of(name, couponType, value, expiredAt);

            // assert
            assertThat(result).isNotNull();
        }

        @DisplayName("할인 값이 0이면, BAD_REQUEST 예외가 발생한다.")
        @Test
        void throwsBadRequestException_whenValueIsZero() {
            // arrange & act
            CoreException result = assertThrows(CoreException.class, () ->
                CouponTemplate.of("쿠폰", CouponType.FIXED, 0, ZonedDateTime.now().plusDays(30))
            );

            // assert
            assertThat(result.getErrorType()).isEqualTo(ErrorType.BAD_REQUEST);
            assertThat(result.getCustomMessage()).isEqualTo("할인 값은 0보다 커야 합니다.");
        }

        @DisplayName("할인 값이 음수이면, BAD_REQUEST 예외가 발생한다.")
        @Test
        void throwsBadRequestException_whenValueIsNegative() {
            // arrange & act
            CoreException result = assertThrows(CoreException.class, () ->
                CouponTemplate.of("쿠폰", CouponType.FIXED, -1, ZonedDateTime.now().plusDays(30))
            );

            // assert
            assertThat(result.getErrorType()).isEqualTo(ErrorType.BAD_REQUEST);
            assertThat(result.getCustomMessage()).isEqualTo("할인 값은 0보다 커야 합니다.");
        }

        @DisplayName("정률 할인 쿠폰의 할인율이 100을 초과하면, BAD_REQUEST 예외가 발생한다.")
        @Test
        void throwsBadRequestException_whenRateValueExceeds100() {
            // arrange & act
            CoreException result = assertThrows(CoreException.class, () ->
                CouponTemplate.of("쿠폰", CouponType.RATE, 101, ZonedDateTime.now().plusDays(30))
            );

            // assert
            assertThat(result.getErrorType()).isEqualTo(ErrorType.BAD_REQUEST);
            assertThat(result.getCustomMessage()).isEqualTo("정률 할인일 때 할일율이 100퍼센트를 초과할 수 없습니다.");
        }

        @DisplayName("정률 할인 쿠폰의 할인율이 100이면, CouponTemplate 이 생성된다.")
        @Test
        void createsCouponTemplate_whenRateValueIs100() {
            // arrange
            ZonedDateTime expiredAt = ZonedDateTime.now().plusDays(30);

            // act
            CouponTemplate result = CouponTemplate.of("100% 할인 쿠폰", CouponType.RATE, 100, expiredAt);

            // assert
            assertThat(result).isNotNull();
        }
    }
}
