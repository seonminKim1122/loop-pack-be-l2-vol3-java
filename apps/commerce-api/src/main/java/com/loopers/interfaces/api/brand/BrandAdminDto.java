package com.loopers.interfaces.api.brand;

import com.loopers.application.brand.BrandInfo;

import java.time.ZonedDateTime;

public class BrandAdminDto {

    public static record RegisterRequest(String name, String description) {

    }

    public static record UpdateRequest(String name, String description) {

    }

    public static record ListResponse(Long id, String name, ZonedDateTime createdAt, ZonedDateTime updatedAt) {
        public static ListResponse from (BrandInfo brandInfo) {
            return new ListResponse(brandInfo.id(), brandInfo.name(), brandInfo.createdAt(), brandInfo.updatedAt());
        }
    }

    public static record DetailResponse(Long id, String name, String description, ZonedDateTime createdAt, ZonedDateTime updatedAt) {
        public static DetailResponse from(BrandInfo brandInfo) {
            return new DetailResponse(brandInfo.id(), brandInfo.name(), brandInfo.description(), brandInfo.createdAt(), brandInfo.updatedAt());
        }
    }
}
