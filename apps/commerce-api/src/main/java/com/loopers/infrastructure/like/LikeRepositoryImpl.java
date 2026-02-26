package com.loopers.infrastructure.like;

import com.loopers.domain.like.Like;
import com.loopers.domain.like.LikeRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RequiredArgsConstructor
@Component
public class LikeRepositoryImpl implements LikeRepository {

    private final LikeJpaRepository jpaRepository;

    @Override
    public void save(Like like) {
        jpaRepository.save(like);
    }

    @Override
    public boolean existsByUserIdAndProductId(Long userId, Long productId) {
        return jpaRepository.existsByUserIdAndProductId(userId, productId);
    }

    @Override
    public void deleteByUserIdAndProductId(Long userId, Long productId) {
        jpaRepository.deleteByUserIdAndProductId(userId, productId);
    }

    @Override
    public List<Like> findAllByUserId(Long userId) {
        return jpaRepository.findAllByUserId(userId);
    }

    @Override
    public long countByProductId(Long productId) {
        return jpaRepository.countByProductId(productId);
    }

    @Override
    public Map<Long, Long> countsByProductIdIn(List<Long> productIds) {
        return jpaRepository.countsByProductIdIn(productIds)
                .stream()
                .collect(Collectors.toMap(
                        row -> (Long) row[0],
                        row -> (Long) row[1]
                ));
    }
}
