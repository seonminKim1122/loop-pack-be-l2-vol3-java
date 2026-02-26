package com.loopers.infrastructure.product;

import com.loopers.domain.product.Product;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface ProductJpaRepository extends JpaRepository<Product, Long> {

    @Modifying
    @Query("DELETE FROM Product p WHERE p.brandId = :brandId")
    void deleteAllByBrandId(@Param("brandId") Long brandId);
}
