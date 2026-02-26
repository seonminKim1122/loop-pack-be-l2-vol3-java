package com.loopers.application.product;

import com.loopers.domain.brand.Brand;
import com.loopers.domain.brand.BrandRepository;
import com.loopers.domain.product.Product;
import com.loopers.domain.product.ProductRepository;
import com.loopers.domain.product.vo.Price;
import com.loopers.domain.product.vo.Stock;
import com.loopers.support.error.CoreException;
import com.loopers.support.error.ErrorType;
import com.loopers.support.web.PageResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@RequiredArgsConstructor
@Component
public class ProductFacade {

    private final BrandRepository brandRepository;
    private final ProductRepository productRepository;

    @Transactional
    public void register(String name, String description, Integer stock, Integer price, Long brandId) {
        Brand brand = brandRepository.findById(brandId)
                .orElseThrow(() -> new CoreException(ErrorType.BAD_REQUEST, "이미 등록된 브랜드로만 상품을 등록할 수 있습니다."));

        Stock stockVo = Stock.from(stock);
        Price priceVo = Price.from(price);

        Product product = Product.of(name, description, stockVo, priceVo, brand.getId());
        productRepository.save(product);
    }

    @Transactional
    public void update(Long productId, String name, String description, Integer stock, Integer price) {
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new CoreException(ErrorType.NOT_FOUND, "존재하지 않는 상품입니다."));

        Stock stockVo = Stock.from(stock);
        Price priceVo = Price.from(price);
        product.update(name, description, stockVo, priceVo);
    }

    @Transactional(readOnly = true)
    public PageResponse<ProductInfo> getList(Pageable pageable) {
        PageResponse<Product> products = productRepository.findAll(pageable);

        return products.map(product -> {
            Optional<Brand> optionalBrand = brandRepository.findById(product.brand());
            if (optionalBrand.isEmpty()) {
                return ProductInfo.of(product, null);
            } else {
                Brand brand = optionalBrand.get();
                return ProductInfo.of(product, brand.name());
            }
        });
    }

    @Transactional(readOnly = true)
    public ProductInfo getDetail(Long productId) {

        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new CoreException(ErrorType.NOT_FOUND, "존재하지 않는 상품입니다."));
        Optional<Brand> optionalBrand = brandRepository.findById(product.brand());

        if (optionalBrand.isEmpty()) {
            return ProductInfo.of(product, null);
        } else {
            Brand brand = optionalBrand.get();
            return ProductInfo.of(product, brand.name());
        }
    }

    @Transactional
    public void delete(Long productId) {
        productRepository.deleteById(productId);
    }
}
