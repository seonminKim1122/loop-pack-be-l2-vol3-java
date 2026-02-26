package com.loopers.interfaces.api.product;

import com.loopers.application.product.ProductFacade;
import com.loopers.interfaces.api.ApiResponse;
import com.loopers.interfaces.auth.AdminOnly;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

@RequiredArgsConstructor
@RestController
public class ProductController {

    private final ProductFacade productFacade;

    @AdminOnly
    @ResponseStatus(HttpStatus.CREATED)
    @PostMapping("/api-admin/v1/products")
    public ApiResponse<Void> register(@RequestBody ProductAdminDto.RegisterRequest request) {
        productFacade.register(request.name(), request.description(), request.stock(), request.price(), request.brandId());
        return ApiResponse.success(null);
    }

    @AdminOnly
    @PutMapping("/api-admin/v1/products/{productId}")
    public ApiResponse<Void> update(@PathVariable Long productId,
                                    @RequestBody ProductAdminDto.UpdateRequest request) {
        productFacade.update(productId, request.name(), request.description(), request.stock(), request.price());
        return ApiResponse.success(null);
    }

}
