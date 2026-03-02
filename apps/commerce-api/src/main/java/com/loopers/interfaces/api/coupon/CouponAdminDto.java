package com.loopers.interfaces.api.coupon;

import com.loopers.application.coupon.CouponTemplateDetailInfo;
import com.loopers.application.coupon.CouponTemplateListInfo;

import java.time.ZonedDateTime;

public class CouponAdminDto {

    public static record TemplateRegisterRequest(String name, String type, int value, ZonedDateTime expiredAt) {

    }

    public static record TemplateRegisterResponse(Long templateId) {

    }

    public static record TemplateUpdateRequest(String name, int value, ZonedDateTime expiredAt) {

    }

    public static record TemplateListResponse(Long id, String name, String type) {

        public static TemplateListResponse from(CouponTemplateListInfo info) {
            return new TemplateListResponse(info.id(), info.name(), info.type());
        }
    }

    public static record TemplateDetailResponse(Long id, String name, String type, int value, ZonedDateTime expiredAt) {

        public static TemplateDetailResponse from(CouponTemplateDetailInfo info) {
            return new TemplateDetailResponse(info.id(), info.name(), info.type(), info.value(), info.expiredAt());
        }
    }
}
