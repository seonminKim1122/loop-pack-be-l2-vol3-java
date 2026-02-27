package com.loopers.application.order;

import com.loopers.domain.brand.Brand;
import com.loopers.domain.order.OrderItem;
import com.loopers.domain.product.Product;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;

@Component
public class OrderAssembler {

    public List<OrderItem> toOrderItems(OrderCommand orderCommand,
                                        Map<Long, Product> productMap,
                                        Map<Long, Brand> brandMap) {
        return orderCommand.items().stream()
                .map(item -> {
                    Product product = productMap.get(item.productId());
                    Brand brand = brandMap.get(product.brandId());
                    return OrderItem.of(product.getId(), product.name(), brand.name(), product.price().value(), item.quantity());
                })
                .toList();
    }
}
