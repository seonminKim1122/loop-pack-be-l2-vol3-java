package com.loopers.application.like;

import com.loopers.domain.product.Product;

public record LikeProductInfo(String name, String description, String brand, long likeCount) {

    public static LikeProductInfo of(Product product, String brand, long likeCount) {
        return new LikeProductInfo(product.name(), product.description(), brand, likeCount);
    }
}
