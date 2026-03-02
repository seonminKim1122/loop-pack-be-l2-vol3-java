package com.loopers.domain.coupon;

public enum CouponType {

    FIXED {
        public long calculate(long amount, int value) {
            return value;
        }
    },
    RATE {
        public long calculate(long amount, int value) {
            return amount * value / 100;
        }
    };

    public static boolean isValid(String value) {
        try {
            Enum.valueOf(CouponType.class, value);
            return true;
        } catch (NullPointerException | IllegalArgumentException e) {
            return false;
        }
    }

    public abstract long calculate(long amount, int value);
}
