package com.loopers.domain.like;

public interface LikeRepository {

    void save(Like like);

    boolean existsByUserIdAndProductId(Long userId, Long productId);
}
