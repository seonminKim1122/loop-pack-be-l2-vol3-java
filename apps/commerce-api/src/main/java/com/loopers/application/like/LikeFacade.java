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
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@RequiredArgsConstructor
@Component
public class LikeFacade {

    private final UserRepository userRepository;
    private final ProductRepository productRepository;
    private final LikeRepository likeRepository;
    private final BrandRepository brandRepository;
    private final LikeAssembler likeAssembler;

    @Transactional
    public void like(Long userId, Long productId) {
        if (!userRepository.existsById(userId)) throw new CoreException(ErrorType.NOT_FOUND, "존재하지 않는 사용자입니다.");

        Optional<Product> optionalProduct = productRepository.findById(productId);
        if (optionalProduct.isEmpty()) {
            throw new CoreException(ErrorType.NOT_FOUND, "존재하지 않는 상품입니다.");
        }

        Product product = optionalProduct.get();

        if (!likeRepository.existsByUserIdAndProductId(userId, productId)) {
            likeRepository.save(Like.of(userId, productId));
            product.increaseLike();
        }
    }

    @Transactional
    public void unlike(Long userId, Long productId) {
        if (!likeRepository.existsByUserIdAndProductId(userId, productId)) return;

        Optional<Product> optionalProduct = productRepository.findById(productId);
        if (optionalProduct.isEmpty()) return;

        Product product = optionalProduct.get();
        likeRepository.deleteByUserIdAndProductId(userId, productId);
        product.decreaseLike();
    }

    @Transactional(readOnly = true)
    public List<LikeProductInfo> getLikeList(Long userId) {
        List<Like> likes = likeRepository.findAllByUserId(userId);
        if (likes.isEmpty()) return List.of();

        List<Long> productIds = likes.stream().map(Like::productId).toList();
        List<Product> products = productRepository.findAllByIdIn(productIds);

        List<Long> brandIds = products.stream().map(Product::brandId).distinct().toList();
        List<Brand> brands = brandRepository.findAllByIdIn(brandIds);

        return likeAssembler.toInfos(products, brands);
    }
}
