package com.loopers.domain.brand;

import com.loopers.support.page.PageResponse;
import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.Optional;

public interface BrandRepository {

    boolean existsByName(String name);

    void save(Brand brand);

    Optional<Brand> findById(Long brandId);

    boolean existsByNameAndIdNot(String name, Long id);

    PageResponse<Brand> findAll(Pageable pageable);

    void deleteById(Long id);

    List<Brand> findAllByIdIn(List<Long> ids);
}
