package com.loopers.domain.product;

import com.loopers.support.web.PageResponse;
import org.springframework.data.domain.Pageable;

import java.util.Optional;

public interface ProductRepository {

    void save(Product product);

    Optional<Product> findById(Long id);

    PageResponse<Product> findAll(Pageable pageable);

    void deleteById(Long id);
}
