package com.loopers.interfaces.api.coupon;

import com.loopers.application.coupon.CouponFacade;
import com.loopers.interfaces.api.ApiResponse;
import com.loopers.interfaces.auth.AuthenticatedUser;
import com.loopers.interfaces.auth.CurrentUser;
import com.loopers.interfaces.auth.LoginRequired;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RequiredArgsConstructor
@RestController
public class CouponController {

    private final CouponFacade couponFacade;

    @ResponseStatus(HttpStatus.CREATED)
    @LoginRequired
    @PostMapping("/api/v1/coupons/{couponId}/issue")
    public ApiResponse<CouponDto.IssueResponse> issue(@PathVariable Long couponId,
                                                      @CurrentUser AuthenticatedUser user) {

        Long issuedCouponId = couponFacade.issue(couponId, user.id());
        return ApiResponse.success(new CouponDto.IssueResponse(issuedCouponId));
    }

}
