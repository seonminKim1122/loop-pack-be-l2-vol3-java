package com.loopers.application.brand;

import com.loopers.domain.brand.Brand;
import com.loopers.domain.brand.BrandRepository;
import com.loopers.support.error.CoreException;
import com.loopers.support.error.ErrorType;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@RequiredArgsConstructor
@Component
public class BrandFacade {

    private final BrandRepository brandRepository;

    @Transactional
    public void register(String name, String description) {
        if (brandRepository.existsByName(name)) {
            throw new CoreException(ErrorType.CONFLICT, "중복된 이름의 브랜드가 존재합니다.");
        }
        Brand brand = Brand.of(name, description);
        brandRepository.save(brand);
    }
}
