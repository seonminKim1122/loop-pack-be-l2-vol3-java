package com.loopers.infrastructure.brand;

import com.loopers.domain.brand.Brand;
import com.loopers.domain.brand.BrandRepository;
import com.loopers.support.error.CoreException;
import com.loopers.support.error.ErrorType;
import com.loopers.support.page.PageResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;

@RequiredArgsConstructor
@Component
public class BrandRepositoryImpl implements BrandRepository {

    private final BrandJpaRepository jpaRepository;

    @Override
    public boolean existsByName(String name) {
        return jpaRepository.existsByName(name);
    }

    @Override
    public void save(Brand brand) {
        try {
            jpaRepository.saveAndFlush(brand);
        } catch (DataIntegrityViolationException e) {
            throw new CoreException(ErrorType.CONFLICT, "중복된 이름의 브랜드가 존재합니다.");
        }
    }

    @Override
    public Optional<Brand> findById(Long brandId) {
        return jpaRepository.findById(brandId);
    }

    @Override
    public boolean existsByNameAndIdNot(String name, Long id) {
        return jpaRepository.existsByNameAndIdNot(name, id);
    }

    @Override
    public PageResponse<Brand> findAll(Pageable pageable) {
        return PageResponse.from(jpaRepository.findAll(pageable));
    }

    @Override
    public void deleteById(Long id) {
        jpaRepository.deleteById(id);
    }

    @Override
    public List<Brand> findAllByIdIn(List<Long> ids) {
        return jpaRepository.findAllByIdIn(ids);
    }
}
