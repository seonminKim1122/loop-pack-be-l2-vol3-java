package com.loopers.interfaces.api.brand;

import com.loopers.application.brand.BrandFacade;
import com.loopers.application.brand.BrandInfo;
import com.loopers.interfaces.api.ApiResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;

@RequiredArgsConstructor
@RestController
public class BrandController {

    private final BrandFacade brandFacade;

    @GetMapping("/api/v1/brands/{brandId}")
    public ApiResponse<BrandDto.DetailResponse> getDetail(@PathVariable Long brandId) {
        BrandInfo brandInfo = brandFacade.getDetail(brandId);
        BrandDto.DetailResponse response = BrandDto.DetailResponse.from(brandInfo);
        return ApiResponse.success(response);
    }
}
