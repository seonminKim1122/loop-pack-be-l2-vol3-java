package com.loopers.domain.product;

import com.loopers.domain.product.vo.Price;
import com.loopers.domain.product.vo.Stock;
import com.loopers.support.error.CoreException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;

class ProductTest {

    @DisplayName("Product 를 생성할 때, ")
    @Nested
    class Create {

        @DisplayName("상품명, 브랜드, 가격, 재고가 모두 주어지면, 정상적으로 생성된다.")
        @Test
        void createsProduct_whenAllRequiredFieldsAreGiven() {
            // arrange
            String name = "나이키 에어맥스";
            Stock stock = Stock.from(10);
            Price price = Price.from(150000);
            Long brandId = 1L;

            // act
            Product product = Product.of(name, null, stock, price, brandId);

            // assert
            assertThat(product).isNotNull();
        }

        @DisplayName("상품설명이 null 이어도, 정상적으로 생성된다.")
        @Test
        void createsProduct_whenDescriptionIsNull() {
            // arrange
            String name = "나이키 에어맥스";
            Stock stock = Stock.from(10);
            Price price = Price.from(150000);
            Long brandId = 1L;

            // act
            Product product = Product.of(name, null, stock, price, brandId);

            // assert
            assertThat(product).isNotNull();
            assertThat(product.description()).isNull();
        }

        @DisplayName("상품명이 null 이면, CoreException 이 발생한다.")
        @Test
        void throwsCoreException_whenNameIsNull() {
            // arrange
            Stock stock = Stock.from(10);
            Price price = Price.from(150000);
            Long brandId = 1L;

            // act
            CoreException result = assertThrows(CoreException.class, () ->
                Product.of(null, "설명", stock, price, brandId)
            );

            // assert
            assertThat(result.getCustomMessage()).isEqualTo("상품명은 필수입니다.");
        }

        @DisplayName("상품명이 blank 이면, CoreException 이 발생한다.")
        @Test
        void throwsCoreException_whenNameIsBlank() {
            // arrange
            Stock stock = Stock.from(10);
            Price price = Price.from(150000);
            Long brandId = 1L;

            // act
            CoreException result = assertThrows(CoreException.class, () ->
                Product.of("   ", "설명", stock, price, brandId)
            );

            // assert
            assertThat(result.getCustomMessage()).isEqualTo("상품명은 필수입니다.");
        }

        @DisplayName("브랜드 ID 가 null 이면, CoreException 이 발생한다.")
        @Test
        void throwsCoreException_whenBrandIdIsNull() {
            // arrange
            String name = "나이키 에어맥스";
            Stock stock = Stock.from(10);
            Price price = Price.from(150000);

            // act
            CoreException result = assertThrows(CoreException.class, () ->
                Product.of(name, "설명", stock, price, null)
            );

            // assert
            assertThat(result.getCustomMessage()).isEqualTo("브랜드는 필수입니다.");
        }
    }

    @DisplayName("Product 를 수정할 때, ")
    @Nested
    class Update {

        @DisplayName("상품명, 가격, 재고가 유효하면, 수정된다.")
        @Test
        void updatesProduct_whenAllRequiredFieldsAreValid() {
            // arrange
            Product product = Product.of("나이키 에어맥스", "설명", Stock.from(10), Price.from(150000), 1L);

            // act & assert
            assertDoesNotThrow(() -> product.update("나이키 줌", "새 설명", Stock.from(20), Price.from(200000)));
        }

        @DisplayName("상품설명이 null 이어도, 수정된다.")
        @Test
        void updatesProduct_whenDescriptionIsNull() {
            // arrange
            Product product = Product.of("나이키 에어맥스", "설명", Stock.from(10), Price.from(150000), 1L);

            // act
            product.update("나이키 줌", null, Stock.from(20), Price.from(200000));

            // assert
            assertThat(product.description()).isNull();
        }

        @DisplayName("상품명이 null 이면, CoreException 이 발생한다.")
        @Test
        void throwsCoreException_whenNameIsNull() {
            // arrange
            Product product = Product.of("나이키 에어맥스", "설명", Stock.from(10), Price.from(150000), 1L);

            // act
            CoreException result = assertThrows(CoreException.class, () ->
                product.update(null, "새 설명", Stock.from(20), Price.from(200000))
            );

            // assert
            assertThat(result.getCustomMessage()).isEqualTo("상품명은 필수입니다.");
        }

        @DisplayName("상품명이 blank 이면, CoreException 이 발생한다.")
        @Test
        void throwsCoreException_whenNameIsBlank() {
            // arrange
            Product product = Product.of("나이키 에어맥스", "설명", Stock.from(10), Price.from(150000), 1L);

            // act
            CoreException result = assertThrows(CoreException.class, () ->
                product.update("   ", "새 설명", Stock.from(20), Price.from(200000))
            );

            // assert
            assertThat(result.getCustomMessage()).isEqualTo("상품명은 필수입니다.");
        }
    }
}
