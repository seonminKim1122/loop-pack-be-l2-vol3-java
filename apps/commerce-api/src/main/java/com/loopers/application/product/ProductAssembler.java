package com.loopers.application.product;

import com.loopers.domain.brand.Brand;
import com.loopers.domain.product.Product;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Component
public class ProductAssembler {

    public List<ProductInfo> toInfos(List<Product> products, List<Brand> brands, Map<Long, Long> likeCounts) {

        Map<Long, Brand> brandMap = brands.stream().collect(Collectors.toMap(Brand::getId, b -> b));

        return products.stream()
                .map(product -> {
                    Brand brand = brandMap.get(product.brandId());
                    long count = likeCounts.get(product.getId());
                    return ProductInfo.of(product, brand != null ? brand.name() : null, count);
                }).toList();
    }
}
