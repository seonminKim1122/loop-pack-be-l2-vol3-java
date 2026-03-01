package com.loopers.domain.order;

import com.loopers.domain.product.Product;
import com.loopers.support.error.CoreException;
import com.loopers.support.error.ErrorType;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Component
public class StockPolicy {

    public void validate(Map<Long, Product> productMap, List<OrderLine> orderLines) {
        List<String> shortages = new ArrayList<>();

        for (OrderLine orderLine : orderLines) {
            Product product = productMap.get(orderLine.productId());

            if (!product.hasEnoughStock(orderLine.quantity())) {
                shortages.add("상품: %s, 요청: %d, 재고: %d".formatted(
                        product.name(), orderLine.quantity(), product.stock().value()));
            }
        }

        if (!shortages.isEmpty()) {
            throw new CoreException(ErrorType.BAD_REQUEST, "재고 부족:\n" + String.join("\n", shortages));
        }
    }
}
