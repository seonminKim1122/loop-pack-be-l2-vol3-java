package com.loopers.infrastructure.product;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.loopers.application.product.ProductCacheStore;
import com.loopers.application.product.ProductInfo;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.TimeUnit;

@RequiredArgsConstructor
@Component
public class RedisProductCacheStore implements ProductCacheStore {

    private static final String DETAIL_KEY_PREFIX = "product:detail:";
    private static final String LIST_KEY_PREFIX = "product:list:";

    private final RedisTemplate<String, String> redisTemplate;
    private final ObjectMapper objectMapper;

    @Override
    public Optional<ProductInfo> get(Long productId) {
        try {
            String cached = redisTemplate.opsForValue().get(DETAIL_KEY_PREFIX + productId);
            if (cached != null) {
                return Optional.of(objectMapper.readValue(cached, ProductInfo.class));
            }
        } catch (Exception ignored) {
        }
        return Optional.empty();
    }

    @Override
    public void put(Long productId, ProductInfo productInfo) {
        try {
            String json = objectMapper.writeValueAsString(productInfo);
            redisTemplate.opsForValue().set(DETAIL_KEY_PREFIX + productId, json, DETAIL_TTL_MINUTES, TimeUnit.MINUTES);
        } catch (Exception ignored) {
        }
    }

    @Override
    public void evict(Long productId) {
        redisTemplate.delete(DETAIL_KEY_PREFIX + productId);
    }

    @Override
    public Optional<ProductListCacheEntry> getList(Long brandId, String sort, int page, int size) {
        try {
            String cached = redisTemplate.opsForValue().get(listKey(brandId, sort, page, size));
            if (cached != null) {
                return Optional.of(objectMapper.readValue(cached, ProductListCacheEntry.class));
            }
        } catch (Exception ignored) {
        }
        return Optional.empty();
    }

    @Override
    public void putList(Long brandId, String sort, int page, int size, List<Long> productIds, int totalPages) {
        try {
            String json = objectMapper.writeValueAsString(new ProductListCacheEntry(productIds, totalPages));
            redisTemplate.opsForValue().set(listKey(brandId, sort, page, size), json, LIST_TTL_SECONDS, TimeUnit.SECONDS);
        } catch (Exception ignored) {
        }
    }

    @Override
    public Map<Long, ProductInfo> multiGet(List<Long> productIds) {
        List<String> keys = productIds.stream()
                .map(id -> DETAIL_KEY_PREFIX + id)
                .toList();
        List<String> values = redisTemplate.opsForValue().multiGet(keys);
        Map<Long, ProductInfo> result = new HashMap<>();
        if (values == null) return result;
        for (int i = 0; i < productIds.size(); i++) {
            String value = values.get(i);
            if (value != null) {
                try {
                    result.put(productIds.get(i), objectMapper.readValue(value, ProductInfo.class));
                } catch (Exception ignored) {
                }
            }
        }
        return result;
    }

    private String listKey(Long brandId, String sort, int page, int size) {
        return LIST_KEY_PREFIX + "brandId=" + brandId + ":sort=" + sort + ":page=" + page + ":size=" + size;
    }
}
