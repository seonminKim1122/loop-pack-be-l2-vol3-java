package com.loopers.domain.like;

import java.util.List;
import java.util.Map;

public interface LikeRepository {

    void save(Like like);

    boolean existsByUserIdAndProductId(Long userId, Long productId);

    void deleteByUserIdAndProductId(Long userId, Long productId);

    void deleteAllByProductId(Long productId);

    void deleteAllByProductIdIn(List<Long> productIds);

    List<Like> findAllByUserId(Long userId);

    long countByProductId(Long productId);

    Map<Long, Long> countsByProductIdIn(List<Long> productIds);
}
