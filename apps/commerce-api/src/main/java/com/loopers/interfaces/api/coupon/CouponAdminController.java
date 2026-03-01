package com.loopers.interfaces.api.coupon;

import com.loopers.application.coupon.CouponFacade;
import com.loopers.interfaces.api.ApiResponse;
import com.loopers.interfaces.auth.AdminOnly;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RequiredArgsConstructor
@RestController
public class CouponAdminController {

    private final CouponFacade couponFacade;

    @AdminOnly
    @ResponseStatus(HttpStatus.CREATED)
    @PostMapping("/api-admin/v1/coupons")
    public ApiResponse<CouponDto.TemplateRegisterResponse> register(@RequestBody CouponDto.TemplateRegisterRequest request) {

        Long templateId = couponFacade.registerTemplate(request.name(), request.type(), request.value(), request.expiredAt());
        CouponDto.TemplateRegisterResponse response = new CouponDto.TemplateRegisterResponse(templateId);
        return ApiResponse.success(response);
    }

}
