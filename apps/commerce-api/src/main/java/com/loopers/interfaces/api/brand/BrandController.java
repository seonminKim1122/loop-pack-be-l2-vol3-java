package com.loopers.interfaces.api.brand;

import com.loopers.application.brand.BrandFacade;
import com.loopers.application.brand.BrandInfo;
import com.loopers.interfaces.api.ApiResponse;
import com.loopers.interfaces.auth.AdminOnly;
import com.loopers.support.web.PageResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

@RequiredArgsConstructor
@RestController
public class BrandController {

    private final BrandFacade brandFacade;

    @AdminOnly
    @ResponseStatus(HttpStatus.CREATED)
    @PostMapping("/api-admin/v1/brands")
    public ApiResponse<Void> register(@RequestBody BrandAdminDto.RegisterRequest request) {
        brandFacade.register(request.name(), request.description());
        return ApiResponse.success(null);
    }

    @AdminOnly
    @PutMapping("/api-admin/v1/brands/{brandId}")
    public ApiResponse<Void> update(@PathVariable Long brandId,
                                    @RequestBody BrandAdminDto.UpdateRequest request) {
        brandFacade.update(brandId, request.name(), request.description());
        return ApiResponse.success(null);
    }

    @AdminOnly
    @GetMapping("/api-admin/v1/brands")
    public ApiResponse<PageResponse<BrandAdminDto.ListResponse>> getList(@RequestParam(name = "page", defaultValue = "0") int page,
                                                                         @RequestParam(name = "size", defaultValue = "20") int size) {
        PageRequest pageRequest = PageRequest.of(page, size);
        PageResponse<BrandInfo> list = brandFacade.getList(pageRequest);
        PageResponse<BrandAdminDto.ListResponse> response = list.map(BrandAdminDto.ListResponse::from);
        return ApiResponse.success(response);
    }

    @AdminOnly
    @GetMapping("/api-admin/v1/brands/{brandId}")
    public ApiResponse<BrandAdminDto.DetailResponse> getDetail(@PathVariable Long brandId) {

        BrandInfo brandInfo = brandFacade.getDetail(brandId);
        BrandAdminDto.DetailResponse response = BrandAdminDto.DetailResponse.from(brandInfo);
        return ApiResponse.success(response);
    }
}
