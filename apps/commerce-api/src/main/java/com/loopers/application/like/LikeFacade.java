package com.loopers.application.like;

import com.loopers.domain.brand.Brand;
import com.loopers.domain.brand.BrandRepository;
import com.loopers.domain.like.Like;
import com.loopers.domain.like.LikeRepository;
import com.loopers.domain.product.Product;
import com.loopers.domain.product.ProductRepository;
import com.loopers.domain.user.UserRepository;
import com.loopers.support.error.CoreException;
import com.loopers.support.error.ErrorType;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RequiredArgsConstructor
@Component
public class LikeFacade {

    private final UserRepository userRepository;
    private final ProductRepository productRepository;
    private final LikeRepository likeRepository;
    private final BrandRepository brandRepository;

    public void like(Long userId, Long productId) {
        if (!userRepository.existsById(userId)) throw new CoreException(ErrorType.NOT_FOUND, "존재하지 않는 사용자입니다.");
        if (!productRepository.existsById(productId)) throw new CoreException(ErrorType.NOT_FOUND, "존재하지 않는 상품입니다.");

        if (!likeRepository.existsByUserIdAndProductId(userId, productId)) {
            likeRepository.save(Like.of(userId, productId));
        }
    }

    public void unlike(Long userId, Long productId) {
        likeRepository.deleteByUserIdAndProductId(userId, productId);
    }

    public List<LikeProductInfo> getLikeList(Long userId) {
        List<Like> likes = likeRepository.findAllByUserId(userId);
        if (likes.isEmpty()) return List.of();

        List<Long> productIds = likes.stream().map(Like::productId).toList();
        List<Product> products = productRepository.findAllByIdIn(productIds);

        List<Long> brandIds = products.stream().map(Product::brand).toList();
        List<Brand> brands = brandRepository.findAllByIdIn(brandIds);

        Map<Long, Long> likeCounts = likeRepository.countsByProductIdIn(productIds);
        Map<Long, Product> productMap = products.stream().collect(Collectors.toMap(Product::getId, p -> p));
        Map<Long, Brand> brandMap = brands.stream().collect(Collectors.toMap(Brand::getId, b -> b));

        return likes.stream()
                .filter(like -> productMap.containsKey(like.productId()))
                .map(like -> {
                    Product product = productMap.get(like.productId());
                    Brand brand = brandMap.get(product.brand());
                    long count = likeCounts.getOrDefault(like.productId(), 0L);
                    return LikeProductInfo.of(product, brand != null ? brand.name() : null, count);
                })
                .toList();
    }
}
