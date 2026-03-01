package com.loopers.application.brand;

import com.loopers.domain.brand.Brand;
import com.loopers.domain.brand.BrandRepository;
import com.loopers.domain.like.LikeRepository;
import com.loopers.domain.product.ProductRepository;
import com.loopers.support.error.CoreException;
import com.loopers.support.error.ErrorType;
import com.loopers.support.page.PageResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@RequiredArgsConstructor
@Component
public class BrandFacade {

    private final BrandRepository brandRepository;
    private final ProductRepository productRepository;
    private final LikeRepository likeRepository;

    @Transactional
    public void register(String name, String description) {
        if (brandRepository.existsByName(name)) {
            throw new CoreException(ErrorType.CONFLICT, "중복된 이름의 브랜드가 존재합니다.");
        }
        Brand brand = Brand.of(name, description);
        brandRepository.save(brand);

    }

    @Transactional
    public void update(Long brandId, String name, String description) {
        Brand brand = brandRepository.findById(brandId)
                .orElseThrow(() -> new CoreException(ErrorType.NOT_FOUND, "존재하지 않는 브랜드입니다."));

        if (brandRepository.existsByNameAndIdNot(name, brandId)) {
            throw new CoreException(ErrorType.CONFLICT, "중복된 이름의 브랜드가 존재합니다.");
        }

        brand.update(name, description);
    }

    @Transactional(readOnly = true)
    public PageResponse<BrandInfo> getList(Pageable pageable) {
        PageResponse<Brand> brands = brandRepository.findAll(pageable);
        return brands.map(BrandInfo::from);
    }

    @Transactional(readOnly = true)
    public BrandInfo getDetail(Long brandId) {
        Brand brand = brandRepository.findById(brandId)
                .orElseThrow(() -> new CoreException(ErrorType.NOT_FOUND, "존재하지 않는 브랜드입니다."));

        return BrandInfo.from(brand);
    }

    @Transactional
    public void delete(Long brandId) {
        List<Long> productIds = productRepository.findAllIdsByBrandId(brandId);
        likeRepository.deleteAllByProductIdIn(productIds);
        productRepository.deleteAllByBrandId(brandId);
        brandRepository.deleteById(brandId);
    }
}
