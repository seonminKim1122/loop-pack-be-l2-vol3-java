package com.loopers.infrastructure.product;

import com.loopers.domain.product.Product;
import jakarta.persistence.LockModeType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface ProductJpaRepository extends JpaRepository<Product, Long> {

    @Query("SELECT p.id FROM Product p WHERE p.brandId = :brandId")
    List<Long> findAllIdsByBrandId(@Param("brandId") Long brandId);

    @Modifying
    @Query("DELETE FROM Product p WHERE p.brandId = :brandId")
    void deleteAllByBrandId(@Param("brandId") Long brandId);

    List<Product> findAllByIdIn(List<Long> ids);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT p FROM Product p WHERE p.id IN :ids")
    List<Product> findAllByIdInWithLock(@Param("ids") List<Long> ids);

    Page<Product> findAllByBrandId(Long brandId, Pageable pageable);

    @Modifying
    @Query("UPDATE Product p SET p.likeCount = p.likeCount + 1 WHERE p.id = :productId")
    void increaseLikeCount(@Param("productId") Long productId);

    @Modifying
    @Query("UPDATE Product p SET p.likeCount = p.likeCount - 1 WHERE p.id = :productId AND p.likeCount > 0")
    void decreaseLikeCount(@Param("productId") Long productId);
}
