package com.loopers.domain.order;

import com.loopers.domain.product.Product;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Component
public class StockPolicy {

    public List<Shortage> findShortages(Map<Long, Product> productMap, List<OrderLine> orderLines) {
        List<Shortage> result = new ArrayList<>();

        for (OrderLine orderLine : orderLines) {
            Product product = productMap.get(orderLine.productId());

            if (!product.hasEnoughStock(orderLine.quantity())) {
                result.add(new Shortage(
                        product.getId(),
                        product.name(),
                        orderLine.quantity(),
                        product.stock().value()
                ));
            }
        }

        return result;
    }

    public static record Shortage(Long productId, String productName, int requested, int stock) {
    }
}
