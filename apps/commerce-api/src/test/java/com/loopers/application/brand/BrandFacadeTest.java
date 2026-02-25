package com.loopers.application.brand;

import com.loopers.domain.brand.BrandRepository;
import com.loopers.support.error.CoreException;
import com.loopers.support.error.ErrorType;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class BrandFacadeTest {

    BrandRepository brandRepository = mock(BrandRepository.class);
    BrandFacade brandFacade = new BrandFacade(brandRepository);

    @DisplayName("브랜드 등록 시, ")
    @Nested
    class Register {

        @DisplayName("중복되지 않는 브랜드명이면, 브랜드를 저장한다.")
        @Test
        void savesBrand_whenNameIsNew() {
            // arrange
            String name = "나이키";
            String description = "Just Do It";
            when(brandRepository.existsByName(name)).thenReturn(false);

            // act
            brandFacade.register(name, description);

            // assert
            verify(brandRepository).save(any());
        }

        @DisplayName("중복된 브랜드명이면, CONFLICT 예외가 발생한다.")
        @Test
        void throwsConflictException_whenNameIsDuplicated() {
            // arrange
            String name = "나이키";
            String description = "Just Do It";
            when(brandRepository.existsByName(name)).thenReturn(true);

            // act
            CoreException result = assertThrows(CoreException.class, () ->
                brandFacade.register(name, description)
            );

            // assert
            assertThat(result.getErrorType()).isEqualTo(ErrorType.CONFLICT);
        }
    }
}
