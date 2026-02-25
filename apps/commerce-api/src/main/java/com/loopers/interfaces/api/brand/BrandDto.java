package com.loopers.interfaces.api.brand;

public class BrandDto {

    public static record RegisterRequest(String name, String description) {

    }

    public static record UpdateRequest(String name, String description) {

    }
}
