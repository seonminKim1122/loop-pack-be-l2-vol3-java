package com.loopers.application.like;

import com.loopers.domain.like.LikeRepository;
import com.loopers.domain.product.ProductRepository;
import com.loopers.domain.user.UserRepository;
import com.loopers.support.error.CoreException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.*;

class LikeFacadeTest {

    UserRepository userRepository = mock(UserRepository.class);
    ProductRepository productRepository = mock(ProductRepository.class);
    LikeRepository likeRepository = mock(LikeRepository.class);

    LikeFacade likeFacade = new LikeFacade(userRepository, productRepository, likeRepository);

    @DisplayName("좋아요 시, ")
    @Nested
    class Like {

        @DisplayName("사용자와 상품이 모두 존재하고 동일한 대상에 대한 좋아요가 없으면 좋아요를 저장한다")
        @Test
        void savesLike_whenUserAndProductExistsAndLikeNotExists() {
            // arrange
            Long userId = 1L;
            Long productId = 1L;
            when(userRepository.existsById(userId)).thenReturn(true);
            when(productRepository.existsById(productId)).thenReturn(true);
            when(likeRepository.existsByUserIdAndProductId(userId, productId)).thenReturn(false);

            // act
            likeFacade.like(userId, productId);

            // assert
            verify(likeRepository).save(any());
        }

        @DisplayName("사용자가 존재하지 않으면, CoreException 이 발생한다.")
        @Test
        void throwsCoreException_whenUserNotFound() {
            // arrange
            Long userId = 1L;
            Long productId = 1L;
            when(userRepository.existsById(userId)).thenReturn(false);
            when(productRepository.existsById(productId)).thenReturn(true);

            // act
            CoreException result = assertThrows(CoreException.class, () -> {
                likeFacade.like(userId, productId);
            });

            // assert
            assertThat(result.getCustomMessage()).isEqualTo("존재하지 않는 사용자입니다.");
        }

        @DisplayName("상품이 존재하지 않으면, CoreException 이 발생한다")
        @Test
        void throwsCoreException_whenProductNotFound() {
            // arrange
            Long userId = 1L;
            Long productId = 1L;
            when(userRepository.existsById(userId)).thenReturn(true);
            when(productRepository.existsById(productId)).thenReturn(false);

            // act
            CoreException result = assertThrows(CoreException.class, () -> {
                likeFacade.like(userId, productId);
            });

            // assert
            assertThat(result.getCustomMessage()).isEqualTo("존재하지 않는 상품입니다.");
        }

        @DisplayName("이미 좋아요한 이력이 있으면, save 하지 않는다")
        void notSavesLike_whenLikeExists() {
            // arrange
            Long userId = 1L;
            Long productId = 1L;
            when(userRepository.existsById(userId)).thenReturn(true);
            when(productRepository.existsById(productId)).thenReturn(true);
            when(likeRepository.existsByUserIdAndProductId(userId, productId)).thenReturn(true);

            // act
            likeFacade.like(userId, productId);

            // assert
            verify(likeRepository, never()).save(any());
        }
    }
}
