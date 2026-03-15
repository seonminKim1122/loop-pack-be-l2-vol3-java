package com.loopers.application.product;

import com.loopers.domain.brand.Brand;
import com.loopers.domain.brand.BrandRepository;
import com.loopers.domain.product.Product;
import com.loopers.domain.product.ProductRepository;
import com.loopers.domain.product.ProductSortType;
import com.loopers.support.error.CoreException;
import com.loopers.support.error.ErrorType;
import com.loopers.support.page.PageResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;

@RequiredArgsConstructor
@Component
public class ProductQueryService {

    private final ProductRepository productRepository;
    private final BrandRepository brandRepository;
    private final ProductAssembler productAssembler;
    private final ProductCacheStore productCacheStore;

    @Transactional(readOnly = true)
    public PageResponse<ProductInfo> getList(Pageable pageable, Long brandId, String sort) {
        int page = pageable.getPageNumber();
        int size = pageable.getPageSize();
        PageRequest pageRequest = PageRequest.of(page, size, ProductSortType.from(sort).getSort());

        return productCacheStore.getList(brandId, sort, page, size)
                .map(entry -> resolveFromCache(entry, page, size))
                .orElseGet(() -> resolveFromDatabase(brandId, pageRequest, sort, page, size));
    }

    private PageResponse<ProductInfo> resolveFromCache(ProductCacheStore.ProductListCacheEntry entry, int page, int size) {
        List<Long> productIds = entry.productIds();
        int totalPages = entry.totalPages();
        if (productIds.isEmpty()) {
            return new PageResponse<>(List.of(), page, size, totalPages);
        }

        Map<Long, ProductInfo> cachedInfos = productCacheStore.multiGet(productIds);
        List<Long> missingIds = productIds.stream().filter(id -> !cachedInfos.containsKey(id)).toList();
        if (missingIds.isEmpty()) {
            return new PageResponse<>(productIds.stream().map(cachedInfos::get).toList(), page, size, totalPages);
        }

        Map<Long, ProductInfo> allInfoMap = new HashMap<>(cachedInfos);
        List<Product> missingProducts = productRepository.findAllByIdIn(missingIds);
        List<Long> missingBrandIds = missingProducts.stream().map(Product::brandId).distinct().toList();
        List<Brand> missingBrands = brandRepository.findAllByIdIn(missingBrandIds);
        Map<Long, ProductInfo> missingInfoMap = productAssembler.toInfoMap(missingProducts, missingBrands);
        allInfoMap.putAll(missingInfoMap);
        missingInfoMap.forEach(productCacheStore::put);

        List<ProductInfo> orderedInfos = productIds.stream()
                .map(allInfoMap::get)
                .filter(Objects::nonNull)
                .toList();

        return new PageResponse<>(orderedInfos, page, size, totalPages);
    }

    private PageResponse<ProductInfo> resolveFromDatabase(Long brandId, PageRequest pageRequest, String sort, int page, int size) {
        PageResponse<Product> products = brandId == null
                ? productRepository.findAll(pageRequest)
                : productRepository.findAllByBrandId(brandId, pageRequest);

        List<Product> productList = products.content();
        if (productList.isEmpty()) {
            productCacheStore.putList(brandId, sort, page, size, List.of(), products.totalPages());
            return new PageResponse<>(List.of(), products.page(), products.size(), products.totalPages());
        }

        List<Long> brandIds = productList.stream().map(Product::brandId).toList();
        List<Brand> brands = brandRepository.findAllByIdIn(brandIds);
        Map<Long, ProductInfo> infoMap = productAssembler.toInfoMap(productList, brands);

        List<Long> productIds = productList.stream().map(Product::getId).toList();
        productCacheStore.putList(brandId, sort, page, size, productIds, products.totalPages());
        infoMap.forEach((id, info) -> productCacheStore.put(id, info));

        List<ProductInfo> orderedInfos = productIds.stream().map(infoMap::get).filter(Objects::nonNull).toList();
        return new PageResponse<>(orderedInfos, products.page(), products.size(), products.totalPages());
    }

    @Transactional(readOnly = true)
    public ProductInfo getDetail(Long productId) {
        Optional<ProductInfo> cached = productCacheStore.get(productId);
        if (cached.isPresent()) {
            return cached.get();
        }

        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new CoreException(ErrorType.NOT_FOUND, "존재하지 않는 상품입니다."));

        Optional<Brand> optionalBrand = brandRepository.findById(product.brandId());
        ProductInfo productInfo = ProductInfo.of(product, optionalBrand.map(Brand::name).orElse(null));

        productCacheStore.put(productId, productInfo);

        return productInfo;
    }

    public void evict(Long productId) {
        productCacheStore.evict(productId);
    }
}
