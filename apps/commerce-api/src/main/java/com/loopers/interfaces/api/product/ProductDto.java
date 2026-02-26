package com.loopers.interfaces.api.product;

import com.loopers.application.product.ProductInfo;

public class ProductDto {

    public static record ListResponse(String name, String brand, int price, long likeCount) {
        public  static ListResponse from(ProductInfo productInfo) {
            return new ListResponse(productInfo.name(), productInfo.brand(), productInfo.price(), productInfo.likeCount());
        }
    }
}
