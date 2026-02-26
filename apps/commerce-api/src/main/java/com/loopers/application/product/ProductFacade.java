package com.loopers.application.product;

import com.loopers.domain.brand.Brand;
import com.loopers.domain.brand.BrandRepository;
import com.loopers.domain.like.LikeRepository;
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

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

@RequiredArgsConstructor
@Component
public class ProductFacade {

    private final BrandRepository brandRepository;
    private final ProductRepository productRepository;
    private final LikeRepository likeRepository;

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
        List<Product> productList = products.content();
        if (productList.isEmpty()) return new PageResponse<>(List.of(), products.page(), products.size(), products.totalPages());

        List<Long> brandIds = productList.stream().map(Product::brand).toList();
        List<Brand> brands = brandRepository.findAllByIdIn(brandIds);
        Map<Long, Brand> brandMap = brands.stream().collect(Collectors.toMap(Brand::getId, b -> b));

        List<Long> productIds = productList.stream().map(Product::getId).toList();
        Map<Long, Long> likeCounts = likeRepository.countsByProductIdIn(productIds);

        List<ProductInfo> productInfos = productList.stream()
                .map(product -> {
                    Brand brand = brandMap.get(product.brand());
                    long count = likeCounts.getOrDefault(product.getId(), 0L);
                    return ProductInfo.of(product, brand != null ? brand.name() : null, count);
                })
                .toList();

        return new PageResponse<>(productInfos, products.page(), products.size(), products.totalPages());
    }

    @Transactional(readOnly = true)
    public ProductInfo getDetail(Long productId) {
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new CoreException(ErrorType.NOT_FOUND, "존재하지 않는 상품입니다."));

        Optional<Brand> optionalBrand = brandRepository.findById(product.brand());
        long likeCount = likeRepository.countByProductId(productId);

        return ProductInfo.of(product, optionalBrand.map(Brand::name).orElse(null), likeCount);
    }

    @Transactional
    public void delete(Long productId) {
        productRepository.deleteById(productId);
    }
}
