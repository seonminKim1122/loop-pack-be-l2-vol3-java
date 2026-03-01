package com.loopers.interfaces.api.product;

import com.loopers.application.product.ProductInfo;

public class ProductAdminDto {

    public static record RegisterRequest(String name, String description, Integer stock, Integer price, Long brandId) {

    }

    public static record UpdateRequest(String name, String description, Integer stock, Integer price) {

    }

    public static record ListResponse(String name, Integer stock, Integer price, String brand) {

        public static ListResponse from(ProductInfo productInfo) {
            return new ListResponse(productInfo.name(), productInfo.stock(), productInfo.price(), productInfo.brand());
        }
    }

    public static record DetailResponse(String name, String description, Integer stock, Integer price, String brand, long likeCount) {

        public static DetailResponse from(ProductInfo productInfo) {
            return new DetailResponse(productInfo.name(), productInfo.description(), productInfo.stock(), productInfo.price(), productInfo.brand(), productInfo.likeCount());
        }
    }
}
