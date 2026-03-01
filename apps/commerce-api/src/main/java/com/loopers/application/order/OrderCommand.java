package com.loopers.application.order;

import java.util.List;

public record OrderCommand(List<Item> items) {

    public static record Item(Long productId, Integer quantity) {}

}
