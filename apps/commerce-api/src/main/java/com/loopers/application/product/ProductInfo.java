package com.loopers.application.product;

import com.loopers.domain.product.Product;

public record ProductInfo(String name, String description, Integer stock, Integer price, String brand) {

    public static ProductInfo of(Product product, String brand) {
        return new ProductInfo(product.name(), product.description(), product.stock().value(), product.price().value(), brand);
    }


}
