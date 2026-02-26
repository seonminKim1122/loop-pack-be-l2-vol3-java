package com.loopers.domain.brand;

import com.loopers.support.web.PageResponse;
import org.springframework.data.domain.Pageable;

import java.util.Optional;

public interface BrandRepository {

    boolean existsByName(String name);

    void save(Brand brand);

    Optional<Brand> findById(Long brandId);

    boolean existsByNameAndIdNot(String name, Long id);

    PageResponse<Brand> findAll(Pageable pageable);

    void deleteById(Long id);
}
