package com.loopers.application.like;

import com.loopers.domain.like.Like;
import com.loopers.domain.like.LikeRepository;
import com.loopers.domain.product.ProductRepository;
import com.loopers.domain.user.UserRepository;
import com.loopers.support.error.CoreException;
import com.loopers.support.error.ErrorType;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@RequiredArgsConstructor
@Component
public class LikeFacade {

    private final UserRepository userRepository;
    private final ProductRepository productRepository;
    private final LikeRepository likeRepository;

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
}
