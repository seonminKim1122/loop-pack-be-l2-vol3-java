package com.loopers.application.like;

import com.loopers.domain.brand.Brand;
import com.loopers.domain.product.Product;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Component
public class LikeAssembler {

    public List<LikeProductInfo> toInfos(List<Product> products, List<Brand> brands, Map<Long, Long> likeCounts) {
        Map<Long, Brand> brandMap = brands.stream().collect(Collectors.toMap(Brand::getId, b -> b));

        return products.stream()
                .map(product -> {
                    Brand brand = brandMap.get(product.brandId());
                    long count = likeCounts.get(product.getId());
                    return LikeProductInfo.of(product, brand.name(), count);
                }).toList();
    }
}
