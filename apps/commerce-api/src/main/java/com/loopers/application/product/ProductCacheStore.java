package com.loopers.application.product;

import java.util.List;
import java.util.Map;
import java.util.Optional;

public interface ProductCacheStore {

    long DETAIL_TTL_MINUTES = 5;
    long LIST_TTL_SECONDS = 30;

    Optional<ProductInfo> get(Long productId);

    void put(Long productId, ProductInfo productInfo);

    void evict(Long productId);

    record ProductListCacheEntry(List<Long> productIds, int totalPages) {}

    Optional<ProductListCacheEntry> getList(Long brandId, String sort, int page, int size);

    void putList(Long brandId, String sort, int page, int size, List<Long> productIds, int totalPages);

    Map<Long, ProductInfo> multiGet(List<Long> productIds);
}
