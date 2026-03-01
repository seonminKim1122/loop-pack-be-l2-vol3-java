package com.loopers.infrastructure.product;

import com.loopers.domain.product.Product;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
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

    Page<Product> findAllByBrandId(Long brandId, Pageable pageable);
}
