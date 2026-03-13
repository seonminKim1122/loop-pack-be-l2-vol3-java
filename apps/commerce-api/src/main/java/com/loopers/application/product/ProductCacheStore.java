package com.loopers.application.product;

import java.util.Optional;

public interface ProductCacheStore {

    Optional<ProductInfo> get(Long productId);

    void put(Long productId, ProductInfo productInfo);

    void evict(Long productId);
}
