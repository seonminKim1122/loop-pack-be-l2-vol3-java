package com.loopers.domain.brand;

public interface BrandRepository {

    boolean existsByName(String name);

    void save(Brand brand);
}
