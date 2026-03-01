package com.loopers.domain.like;

import com.loopers.support.error.CoreException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

class LikeTest {

    @DisplayName("Like 를 생성할 때,")
    @Nested
    class Create {

        @DisplayName("사용자ID, 상품ID가 모두 주어지면, 정상적으로 생성된다.")
        @Test
        void createsLike_whenAllRequiredFilesAreGiven() {
            // arrange
            Long userId = 1L;
            Long productId = 1L;

            // act
            Like like = Like.of(userId, productId);

            // assert
            assertThat(like).isNotNull();
        }

        @DisplayName("사용자ID가 null 이면, CoreException 이 발생한다.")
        @Test
        void throwsCoreException_whenUserIdIsNull() {
            // arrange
            Long userId = null;
            Long productId = 1L;

            // act
            CoreException result = assertThrows(CoreException.class, () -> {
                Like.of(userId, productId);
            });

            // assert
            assertThat(result.getCustomMessage()).isEqualTo("사용자ID는 필수입니다.");
        }

        @DisplayName("상품ID가 null 이면, CoreException 이 발생한다.")
        @Test
        void throwsCoreException_whenProductIdIsNull() {
            // arrange
            Long userId = 1L;
            Long productId = null;

            // act
            CoreException result = assertThrows(CoreException.class, () -> {
                Like.of(userId, productId);
            });

            // assert
            assertThat(result.getCustomMessage()).isEqualTo("상품ID는 필수입니다.");
        }
    }
}
