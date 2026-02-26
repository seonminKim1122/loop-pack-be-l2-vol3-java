package com.loopers.interfaces.api.product;

public class ProductAdminDto {

    public static record RegisterRequest(String name, String description, Integer stock, Integer price, Long brandId) {

    }
}
