package com.loopers.application.brand;

import com.loopers.domain.brand.*;
import com.loopers.support.error.CoreException;
import com.loopers.support.web.PageResponse;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.Optional;

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
            assertThat(result.getCustomMessage()).isEqualTo("중복된 이름의 브랜드가 존재합니다.");
        }
    }

    @DisplayName("브랜드 수정 시, ")
    @Nested
    class Update {

        @DisplayName("존재하는 브랜드이고 중복 없는 이름이면, 브랜드를 수정한다.")
        @Test
        void updatesBrand_whenBrandExistsAndNameIsNotDuplicated() {
            // arrange
            Long brandId = 1L;
            String name = "아디다스";
            String description = "Impossible is Nothing";
            Brand brand = mock(Brand.class);
            when(brandRepository.findById(brandId)).thenReturn(Optional.of(brand));
            when(brandRepository.existsByNameAndIdNot(name, brandId)).thenReturn(false);

            // act
            brandFacade.update(brandId, name, description);

            // assert
            verify(brand).update(name, description);
        }

        @DisplayName("존재하지 않는 brandId 이면, NOT_FOUND 예외가 발생한다.")
        @Test
        void throwsNotFoundException_whenBrandNotFound() {
            // arrange
            Long brandId = 999L;
            String name = "아디다스";
            String description = "Impossible is Nothing";
            when(brandRepository.findById(brandId)).thenReturn(Optional.empty());

            // act
            CoreException result = assertThrows(CoreException.class, () ->
                brandFacade.update(brandId, name, description)
            );

            // assert
            assertThat(result.getCustomMessage()).isEqualTo("존재하지 않는 브랜드입니다.");
        }

        @DisplayName("다른 브랜드와 이름이 중복되면, CONFLICT 예외가 발생한다.")
        @Test
        void throwsConflictException_whenNameIsDuplicated() {
            // arrange
            Long brandId = 1L;
            String name = "아디다스";
            String description = "Impossible is Nothing";
            Brand brand = mock(Brand.class);
            when(brandRepository.findById(brandId)).thenReturn(Optional.of(brand));
            when(brandRepository.existsByNameAndIdNot(name, brandId)).thenReturn(true);

            // act
            CoreException result = assertThrows(CoreException.class, () ->
                brandFacade.update(brandId, name, description)
            );

            // assert
            assertThat(result.getCustomMessage()).isEqualTo("중복된 이름의 브랜드가 존재합니다.");
        }
    }

    @DisplayName("브랜드 목록 조회 시, ")
    @Nested
    class GetList {

        @DisplayName("브랜드 목록을 반환한다.")
        @Test
        void returnsBrandList() {
            // arrange
            Brand brand = Brand.of("나이키", "Just Do It");
            Pageable pageable = PageRequest.of(0, 20);
            PageResponse<Brand> pageResponse = new PageResponse<>(List.of(brand), 0, 20, 1);
            when(brandRepository.findAll(pageable)).thenReturn(pageResponse);

            // act
            PageResponse<BrandInfo> result = brandFacade.getList(pageable);

            // assert
            assertThat(result.content()).hasSize(1);
            assertThat(result.content().get(0).name()).isEqualTo("나이키");
        }
    }

    @DisplayName("브랜드 상세 조회 시, ")
    @Nested
    class GetDetail {

        @DisplayName("존재하는 brandId 이면, BrandInfo 를 반환한다.")
        @Test
        void returnsBrandInfo_whenBrandExists() {
            // arrange
            Long brandId = 1L;
            Brand brand = mock(Brand.class);
            when(brand.getId()).thenReturn(1L);
            when(brand.name()).thenReturn("나이키");
            when(brand.description()).thenReturn("Just Do It");
            when(brandRepository.findById(brandId)).thenReturn(Optional.of(brand));

            // act
            BrandInfo result = brandFacade.getDetail(brandId);

            // assert
            assertThat(result.id()).isEqualTo(1L);
            assertThat(result.name()).isEqualTo("나이키");
            assertThat(result.description()).isEqualTo("Just Do It");
        }

        @DisplayName("존재하지 않는 brandId 이면, 예외가 발생한다.")
        @Test
        void throwsNotFoundException_whenBrandNotFound() {
            // arrange
            Long brandId = 999L;
            when(brandRepository.findById(brandId)).thenReturn(Optional.empty());

            // act
            CoreException result = assertThrows(CoreException.class, () ->
                brandFacade.getDetail(brandId)
            );

            // assert
            assertThat(result.getCustomMessage()).isEqualTo("존재하지 않는 브랜드입니다.");
        }
    }
}
