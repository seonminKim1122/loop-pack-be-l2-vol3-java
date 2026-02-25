package com.loopers.domain.brand;

import java.util.Optional;

public interface BrandRepository {

    boolean existsByName(String name);

    void save(Brand brand);

    Optional<Brand> findById(Long brandId);

    boolean existsByNameAndIdNot(String name, Long id);
}
