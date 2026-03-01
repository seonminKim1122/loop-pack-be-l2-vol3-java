package com.loopers.interfaces.api.brand;

import com.loopers.application.brand.BrandInfo;

public class BrandDto {

    public static record DetailResponse(String name, String description) {
        public static DetailResponse from(BrandInfo brandInfo) {
            return new DetailResponse(brandInfo.name(), brandInfo.description());
        }
    }
}
