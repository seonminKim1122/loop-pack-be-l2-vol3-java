package com.loopers.domain.order;

import com.loopers.domain.product.Product;
import com.loopers.domain.product.vo.Price;
import com.loopers.domain.product.vo.Stock;
import com.loopers.support.error.CoreException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThatNoException;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.assertj.core.api.Assertions.assertThat;

class StockPolicyTest {

    StockPolicy stockPolicy = new StockPolicy();

    @DisplayName("재고 검증 시, ")
    @Nested
    class Validate {

        @DisplayName("모든 상품의 재고가 충분하면, 예외가 발생하지 않는다.")
        @Test
        void doesNotThrow_whenAllStockSufficient() {
            // arrange
            Product product = Product.of("나이키 에어맥스", "설명", Stock.from(10), Price.from(150000), 1L);
            Map<Long, Product> productMap = Map.of(product.getId(), product);
            List<OrderLine> orderLines = List.of(new OrderLine(product.getId(), 5));

            // act & assert
            assertThatNoException().isThrownBy(() -> stockPolicy.validate(productMap, orderLines));
        }

        @DisplayName("재고가 부족한 상품이 있으면, CoreException 이 발생한다.")
        @Test
        void throwsCoreException_whenStockInsufficient() {
            // arrange
            Product product = Product.of("나이키 에어맥스", "설명", Stock.from(2), Price.from(150000), 1L);
            Map<Long, Product> productMap = Map.of(product.getId(), product);
            List<OrderLine> orderLines = List.of(new OrderLine(product.getId(), 5));

            // act
            CoreException result = assertThrows(CoreException.class,
                    () -> stockPolicy.validate(productMap, orderLines));

            // assert
            assertThat(result.getCustomMessage()).contains("재고 부족:\n상품: 나이키 에어맥스, 요청: 5, 재고: 2");
        }
    }
}
