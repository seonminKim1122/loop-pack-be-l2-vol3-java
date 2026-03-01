package com.loopers.domain.brand;

import com.loopers.support.error.CoreException;
import com.loopers.support.error.ErrorType;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;

class BrandTest {

    @DisplayName("Brand 를 생성할 때, ")
    @Nested
    class Create {

        @DisplayName("브랜드명과 설명을 입력하면, Brand 가 생성된다.")
        @Test
        void createsBrand_whenNameAndDescriptionAreValid() {
            // arrange
            String name = "나이키";
            String description = "Just Do It";

            // act
            Brand brand = Brand.of(name, description);

            // assert
            assertThat(brand).isNotNull();
        }

        @DisplayName("설명이 null 이어도, Brand 가 생성된다.")
        @Test
        void createsBrand_whenDescriptionIsNull() {
            // arrange
            String name = "나이키";

            // act
            Brand brand = Brand.of(name, null);

            // assert
            assertThat(brand).isNotNull();
        }

        @DisplayName("브랜드명이 null 이면, BAD_REQUEST 예외가 발생한다.")
        @Test
        void throwsBadRequestException_whenNameIsNull() {
            // arrange & act
            CoreException result = assertThrows(CoreException.class, () ->
                Brand.of(null, "설명")
            );

            // assert
            assertThat(result.getErrorType()).isEqualTo(ErrorType.BAD_REQUEST);
            assertThat(result.getCustomMessage()).isEqualTo("브랜드명은 필수 입력사항입니다.");
        }

        @DisplayName("브랜드명이 빈 문자열이면, BAD_REQUEST 예외가 발생한다.")
        @Test
        void throwsBadRequestException_whenNameIsEmpty() {
            // arrange & act
            CoreException result = assertThrows(CoreException.class, () ->
                Brand.of("", "설명")
            );

            // assert
            assertThat(result.getErrorType()).isEqualTo(ErrorType.BAD_REQUEST);
            assertThat(result.getCustomMessage()).isEqualTo("브랜드명은 필수 입력사항입니다.");
        }

        @DisplayName("브랜드명이 공백이면, BAD_REQUEST 예외가 발생한다.")
        @Test
        void throwsBadRequestException_whenNameIsBlank() {
            // arrange & act
            CoreException result = assertThrows(CoreException.class, () ->
                Brand.of("   ", "설명")
            );

            // assert
            assertThat(result.getErrorType()).isEqualTo(ErrorType.BAD_REQUEST);
            assertThat(result.getCustomMessage()).isEqualTo("브랜드명은 필수 입력사항입니다.");
        }
    }

    @DisplayName("Brand 를 수정할 때, ")
    @Nested
    class Update {

        @DisplayName("브랜드명과 설명을 입력하면, 수정된다.")
        @Test
        void updatesBrand_whenNameAndDescriptionAreValid() {
            // arrange
            Brand brand = Brand.of("나이키", "Just Do It");
            String newName = "아디다스";
            String newDescription = "Impossible is Nothing";

            // act & assert
            assertDoesNotThrow(() -> brand.update(newName, newDescription));
        }

        @DisplayName("설명이 null 이어도, 수정된다.")
        @Test
        void updatesBrand_whenDescriptionIsNull() {
            // arrange
            Brand brand = Brand.of("나이키", "Just Do It");
            String newName = "아디다스";

            // act & assert
            assertDoesNotThrow(() -> brand.update(newName, null));
        }

        @DisplayName("브랜드명이 null 이면, BAD_REQUEST 예외가 발생한다.")
        @Test
        void throwsBadRequestException_whenNameIsNull() {
            // arrange
            Brand brand = Brand.of("나이키", "Just Do It");

            // act
            CoreException result = assertThrows(CoreException.class, () ->
                brand.update(null, "설명")
            );

            // assert
            assertThat(result.getErrorType()).isEqualTo(ErrorType.BAD_REQUEST);
            assertThat(result.getCustomMessage()).isEqualTo("브랜드명은 필수 입력사항입니다.");
        }

        @DisplayName("브랜드명이 공백이면, BAD_REQUEST 예외가 발생한다.")
        @Test
        void throwsBadRequestException_whenNameIsBlank() {
            // arrange
            Brand brand = Brand.of("나이키", "Just Do It");

            // act
            CoreException result = assertThrows(CoreException.class, () ->
                brand.update("   ", "설명")
            );

            // assert
            assertThat(result.getErrorType()).isEqualTo(ErrorType.BAD_REQUEST);
            assertThat(result.getCustomMessage()).isEqualTo("브랜드명은 필수 입력사항입니다.");
        }
    }
}
