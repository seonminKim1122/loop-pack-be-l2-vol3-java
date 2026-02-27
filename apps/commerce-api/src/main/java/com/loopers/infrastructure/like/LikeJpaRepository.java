package com.loopers.infrastructure.like;

import com.loopers.domain.like.Like;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface LikeJpaRepository extends JpaRepository<Like, Long> {

    boolean existsByUserIdAndProductId(Long userId, Long productId);

    void deleteByUserIdAndProductId(Long userId, Long productId);

    void deleteAllByProductIdIn(List<Long> productIds);

    List<Like> findAllByUserId(Long userId);

    long countByProductId(Long productId);

    @Query("SELECT l.productId, COUNT(l) FROM Like l WHERE l.productId IN :productIds GROUP BY l.productId")
    List<Object[]> countsByProductIdIn(@Param("productIds") List<Long> productIds);
}
