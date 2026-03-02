package com.loopers.interfaces.api.coupon;

import com.loopers.application.coupon.CouponFacade;
import com.loopers.application.coupon.CouponTemplateDetailInfo;
import com.loopers.interfaces.api.ApiResponse;
import com.loopers.interfaces.auth.AdminOnly;
import com.loopers.support.page.PageResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;


@RequiredArgsConstructor
@RestController
public class CouponAdminController {

    private final CouponFacade couponFacade;

    @AdminOnly
    @ResponseStatus(HttpStatus.CREATED)
    @PostMapping("/api-admin/v1/coupons")
    public ApiResponse<CouponAdminDto.TemplateRegisterResponse> register(@RequestBody CouponAdminDto.TemplateRegisterRequest request) {

        Long templateId = couponFacade.registerTemplate(request.name(), request.type(), request.value(), request.expiredAt());
        CouponAdminDto.TemplateRegisterResponse response = new CouponAdminDto.TemplateRegisterResponse(templateId);
        return ApiResponse.success(response);
    }

    @AdminOnly
    @PutMapping("/api-admin/v1/coupons/{couponId}")
    public ApiResponse<Void> update(@PathVariable Long couponId,
                                    @RequestBody CouponAdminDto.TemplateUpdateRequest request) {

        couponFacade.updateTemplate(couponId, request.name(), request.value(), request.expiredAt());
        return ApiResponse.success(null);
    }

    @AdminOnly
    @DeleteMapping("/api-admin/v1/coupons/{couponId}")
    public ApiResponse<Void> delete(@PathVariable Long couponId) {
        couponFacade.delete(couponId);
        return ApiResponse.success(null);
    }

    @AdminOnly
    @GetMapping("/api-admin/v1/coupons")
    public ApiResponse<PageResponse<CouponAdminDto.TemplateListResponse>> getList(
            @RequestParam(name = "page", defaultValue = "0") int page,
            @RequestParam(name = "size", defaultValue = "20") int size) {

        PageResponse<CouponAdminDto.TemplateListResponse> response = couponFacade.getList(PageRequest.of(page, size))
                .map(CouponAdminDto.TemplateListResponse::from);
        return ApiResponse.success(response);
    }

    @AdminOnly
    @GetMapping("/api-admin/v1/coupons/{couponId}")
    public ApiResponse<CouponAdminDto.TemplateDetailResponse> getDetail(@PathVariable Long couponId) {

        CouponTemplateDetailInfo info = couponFacade.getDetail(couponId);
        CouponAdminDto.TemplateDetailResponse response = CouponAdminDto.TemplateDetailResponse.from(info);
        return ApiResponse.success(response);
    }
}
