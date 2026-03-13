package com.loopers.infrastructure.product;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.loopers.application.product.ProductCacheStore;
import com.loopers.application.product.ProductInfo;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;

import java.util.Optional;
import java.util.concurrent.TimeUnit;

@RequiredArgsConstructor
@Component
public class RedisProductCacheStore implements ProductCacheStore {

    private static final String KEY_PREFIX = "product:detail:";
    private static final long TTL_MINUTES = 5;

    private final RedisTemplate<String, String> redisTemplate;
    private final ObjectMapper objectMapper;

    @Override
    public Optional<ProductInfo> get(Long productId) {
        try {
            String cached = redisTemplate.opsForValue().get(KEY_PREFIX + productId);
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
            redisTemplate.opsForValue().set(KEY_PREFIX + productId, json, TTL_MINUTES, TimeUnit.MINUTES);
        } catch (Exception ignored) {
        }
    }

    @Override
    public void evict(Long productId) {
        redisTemplate.delete(KEY_PREFIX + productId);
    }
}
