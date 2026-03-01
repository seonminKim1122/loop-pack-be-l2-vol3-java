package com.loopers.domain.coupon;

public enum CouponType {

    FIXED,
    RATE;

    public static boolean isValid(String value) {
        try {
            Enum.valueOf(CouponType.class, value);
            return true;
        } catch (NullPointerException | IllegalArgumentException e) {
            return false;
        }
    }
}
