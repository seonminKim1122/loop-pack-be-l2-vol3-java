package com.loopers.interfaces.api.coupon;

import java.time.ZonedDateTime;

public class CouponDto {

    public static record TemplateRegisterRequest(String name, String type, int value, ZonedDateTime expiredAt) {

    }

    public static record TemplateRegisterResponse(Long templateId) {

    }
}
