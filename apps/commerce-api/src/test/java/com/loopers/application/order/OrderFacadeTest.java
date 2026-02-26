package com.loopers.application.order;

import com.loopers.domain.brand.Brand;
import com.loopers.domain.brand.BrandRepository;
import com.loopers.domain.product.Product;
import com.loopers.domain.product.ProductRepository;
import com.loopers.domain.product.vo.Price;
import com.loopers.domain.product.vo.Stock;
import com.loopers.domain.user.User;
import com.loopers.domain.user.UserRepository;
import com.loopers.support.error.CoreException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class OrderFacadeTest {

    UserRepository userRepository = mock(UserRepository.class);
    ProductRepository productRepository = mock(ProductRepository.class);
    BrandRepository brandRepository = mock(BrandRepository.class);
    com.loopers.domain.order.OrderRepository orderRepository = mock(com.loopers.domain.order.OrderRepository.class);
    OrderFacade orderFacade = new OrderFacade(userRepository, productRepository, brandRepository, orderRepository);

    @DisplayName("주문 생성 시, ")
    @Nested
    class CreateOrder {

        @DisplayName("유효한 사용자와 상품이면, 재고가 차감되고 orderId 를 반환한다.")
        @Test
        void returnsOrderId_andDecreasesStock_whenValidUserAndProducts() {
            // arrange
            Long userId = 1L;
            Long productId = 0L; // BaseEntity id = 0L
            Long brandId = 0L;   // BaseEntity id = 0L

            User user = mock(User.class);
            Brand brand = mock(Brand.class);
            when(brand.getId()).thenReturn(brandId);
            when(brand.name()).thenReturn("나이키");

            Product product = Product.of("나이키 에어맥스", "설명", Stock.from(10), Price.from(150000), brandId);

            when(userRepository.findById(userId)).thenReturn(Optional.of(user));
            when(productRepository.findAllByIdIn(List.of(productId))).thenReturn(List.of(product));
            when(brandRepository.findAllByIdIn(any())).thenReturn(List.of(brand));
            when(orderRepository.save(any())).thenReturn(1L);

            OrderCommand command = new OrderCommand(List.of(new OrderCommand.Item(productId, 3)));

            // act
            Long orderId = orderFacade.createOrder(userId, command);

            // assert
            assertThat(orderId).isEqualTo(1L);
            assertThat(product.stock().value()).isEqualTo(7); // 10 - 3
        }

        @DisplayName("존재하지 않는 사용자이면, CoreException 이 발생한다.")
        @Test
        void throwsCoreException_whenUserNotFound() {
            // arrange
            Long userId = 999L;
            when(userRepository.findById(userId)).thenReturn(Optional.empty());

            OrderCommand command = new OrderCommand(List.of(new OrderCommand.Item(1L, 1)));

            // act
            CoreException result = assertThrows(CoreException.class, () ->
                orderFacade.createOrder(userId, command)
            );

            // assert
            assertThat(result.getCustomMessage()).isEqualTo("사용자 정보를 찾을 수 없습니다.");
        }

        @DisplayName("존재하지 않는 상품이 포함된 경우, CoreException 이 발생한다.")
        @Test
        void throwsCoreException_whenProductNotFound() {
            // arrange
            Long userId = 1L;
            User user = mock(User.class);
            when(userRepository.findById(userId)).thenReturn(Optional.of(user));
            when(productRepository.findAllByIdIn(List.of(999L))).thenReturn(List.of());

            OrderCommand command = new OrderCommand(List.of(new OrderCommand.Item(999L, 1)));

            // act
            CoreException result = assertThrows(CoreException.class, () ->
                orderFacade.createOrder(userId, command)
            );

            // assert
            assertThat(result.getCustomMessage()).isEqualTo("등록되지 않은 상품입니다.");
        }

        @DisplayName("재고가 부족한 상품이 있으면, CoreException 이 발생한다.")
        @Test
        void throwsCoreException_whenStockIsInsufficient() {
            // arrange
            Long userId = 1L;
            Long productId = 0L; // BaseEntity id = 0L

            User user = mock(User.class);
            Product product = Product.of("나이키 에어맥스", "설명", Stock.from(2), Price.from(150000), 1L);

            when(userRepository.findById(userId)).thenReturn(Optional.of(user));
            when(productRepository.findAllByIdIn(List.of(productId))).thenReturn(List.of(product));

            OrderCommand command = new OrderCommand(List.of(new OrderCommand.Item(productId, 5)));

            // act
            CoreException result = assertThrows(CoreException.class, () ->
                orderFacade.createOrder(userId, command)
            );

            // assert
            assertThat(result.getCustomMessage()).contains("재고가 부족한 상품이 포함되어 있습니다");
            assertThat(result.getCustomMessage()).contains("나이키 에어맥스");
        }
    }
}
