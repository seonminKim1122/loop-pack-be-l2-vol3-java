package com.loopers.interfaces.api.brand;

import com.loopers.application.brand.BrandFacade;
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
public class BrandController {

    private final BrandFacade brandFacade;

    @AdminOnly
    @ResponseStatus(HttpStatus.CREATED)
    @PostMapping("/api-admin/v1/brands")
    public ApiResponse<Void> register(@RequestBody BrandDto.RegisterRequest request) {
        brandFacade.register(request.name(), request.description());
        return ApiResponse.success(null);
    }
}
