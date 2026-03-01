package com.loopers.application.brand;

import com.loopers.domain.brand.Brand;

import java.time.ZonedDateTime;

public record BrandInfo(Long id, String name, String description, ZonedDateTime createdAt, ZonedDateTime updatedAt) {

    public static BrandInfo from(Brand brand) {
        return new BrandInfo(brand.getId(), brand.name(), brand.description(), brand.getCreatedAt(), brand.getUpdatedAt());
    }
}
