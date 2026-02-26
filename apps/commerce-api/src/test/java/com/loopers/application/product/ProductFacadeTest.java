package com.loopers.application.product;

import com.loopers.domain.brand.Brand;
import com.loopers.domain.brand.BrandRepository;
import com.loopers.domain.product.Product;
import com.loopers.domain.product.ProductRepository;
import com.loopers.domain.product.vo.Price;
import com.loopers.domain.product.vo.Stock;
import com.loopers.support.error.CoreException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class ProductFacadeTest {

    BrandRepository brandRepository = mock(BrandRepository.class);
    ProductRepository productRepository = mock(ProductRepository.class);
    ProductFacade productFacade = new ProductFacade(brandRepository, productRepository);

    @DisplayName("상품 등록 시, ")
    @Nested
    class Register {

        @DisplayName("존재하는 브랜드이면, 상품을 저장한다.")
        @Test
        void savesProduct_whenBrandExists() {
            // arrange
            Long brandId = 1L;
            Brand brand = mock(Brand.class);
            when(brand.getId()).thenReturn(brandId);
            when(brandRepository.findById(brandId)).thenReturn(Optional.of(brand));

            // act
            productFacade.register("나이키 에어맥스", "설명", 10, 150000, brandId);

            // assert
            verify(productRepository).save(any());
        }

        @DisplayName("존재하지 않는 브랜드이면, CoreException 이 발생한다.")
        @Test
        void throwsCoreException_whenBrandNotFound() {
            // arrange
            Long brandId = 999L;
            when(brandRepository.findById(brandId)).thenReturn(Optional.empty());

            // act
            CoreException result = assertThrows(CoreException.class, () ->
                productFacade.register("나이키 에어맥스", "설명", 10, 150000, brandId)
            );

            // assert
            assertThat(result.getCustomMessage()).isEqualTo("이미 등록된 브랜드로만 상품을 등록할 수 있습니다.");
        }
    }

    @DisplayName("상품 수정 시, ")
    @Nested
    class Update {

        @DisplayName("존재하는 상품이면, 상품 정보를 수정한다.")
        @Test
        void updatesProduct_whenProductExists() {
            // arrange
            Long productId = 1L;
            Product product = mock(Product.class);
            when(productRepository.findById(productId)).thenReturn(Optional.of(product));

            // act
            productFacade.update(productId, "나이키 줌", "새 설명", 20, 200000);

            // assert
            verify(product).update(eq("나이키 줌"), eq("새 설명"), eq(Stock.from(20)), eq(Price.from(200000)));
        }

        @DisplayName("존재하지 않는 상품이면, CoreException 이 발생한다.")
        @Test
        void throwsCoreException_whenProductNotFound() {
            // arrange
            Long productId = 999L;
            when(productRepository.findById(productId)).thenReturn(Optional.empty());

            // act
            CoreException result = assertThrows(CoreException.class, () ->
                productFacade.update(productId, "나이키 줌", "새 설명", 20, 200000)
            );

            // assert
            assertThat(result.getCustomMessage()).isEqualTo("존재하지 않는 상품입니다.");
        }
    }
}
