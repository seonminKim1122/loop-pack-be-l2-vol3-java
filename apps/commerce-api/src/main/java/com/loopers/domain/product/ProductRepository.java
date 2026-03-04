package com.loopers.domain.product;

import com.loopers.support.page.PageResponse;
import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.Optional;

public interface ProductRepository {

    void save(Product product);

    Optional<Product> findById(Long id);

    PageResponse<Product> findAll(Pageable pageable);

    void deleteById(Long id);

    List<Long> findAllIdsByBrandId(Long brandId);

    void deleteAllByBrandId(Long brandId);

    boolean existsById(Long id);

    List<Product> findAllByIdIn(List<Long> ids);

    List<Product> findAllByIdInWithLock(List<Long> ids);

    PageResponse<Product> findAllByBrandId(Long brandId, Pageable pageable);

    void increaseLikeCount(Long productId);

    void decreaseLikeCount(Long productId);
}
