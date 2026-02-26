package com.loopers.application.order;

import com.loopers.domain.brand.Brand;
import com.loopers.domain.brand.BrandRepository;
import com.loopers.domain.order.Order;
import com.loopers.domain.order.OrderItem;
import com.loopers.domain.order.OrderRepository;
import com.loopers.domain.product.Product;
import com.loopers.domain.product.ProductRepository;
import com.loopers.domain.user.UserRepository;
import com.loopers.support.error.CoreException;
import com.loopers.support.error.ErrorType;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

@RequiredArgsConstructor
@Component
public class OrderFacade {

    private final UserRepository userRepository;
    private final ProductRepository productRepository;
    private final BrandRepository brandRepository;
    private final OrderRepository orderRepository;

    @Transactional
    public Long createOrder(Long userId, OrderCommand orderCommand) {
        userRepository.findById(userId)
                .orElseThrow(() -> new CoreException(ErrorType.NOT_FOUND, "사용자 정보를 찾을 수 없습니다."));

        List<Long> productIds = orderCommand.items().stream()
                .map(OrderCommand.Item::productId)
                .toList();

        List<Product> products = productRepository.findAllByIdIn(productIds);

        Set<Long> foundIds = products.stream().map(Product::getId).collect(Collectors.toSet());
        boolean hasNotFound = productIds.stream().anyMatch(id -> !foundIds.contains(id));
        if (hasNotFound) {
            throw new CoreException(ErrorType.NOT_FOUND, "등록되지 않은 상품입니다.");
        }

        Map<Long, Product> productMap = products.stream()
                .collect(Collectors.toMap(Product::getId, p -> p));

        List<OrderCommand.Item> insufficientItems = orderCommand.items().stream()
                .filter(item -> !productMap.get(item.productId()).hasEnoughStock(item.quantity()))
                .toList();

        if (!insufficientItems.isEmpty()) {
            String detail = insufficientItems.stream()
                    .map(item -> {
                        Product p = productMap.get(item.productId());
                        return "상품: %s, 주문수량: %d, 재고수량: %d".formatted(p.name(), item.quantity(), p.stock().value());
                    })
                    .collect(Collectors.joining("\n"));
            throw new CoreException(ErrorType.BAD_REQUEST, "재고가 부족한 상품이 포함되어 있습니다\n" + detail);
        }

        List<Long> brandIds = products.stream().map(Product::brand).toList();
        Map<Long, Brand> brandMap = brandRepository.findAllByIdIn(brandIds).stream()
                .collect(Collectors.toMap(Brand::getId, b -> b));

        orderCommand.items().forEach(item ->
                productMap.get(item.productId()).decreaseStock(item.quantity())
        );

        List<OrderItem> orderItems = orderCommand.items().stream()
                .map(item -> {
                    Product product = productMap.get(item.productId());
                    Brand brand = brandMap.get(product.brand());
                    return OrderItem.of(product.getId(), product.name(), brand != null ? brand.name() : null, product.price().value(), item.quantity());
                })
                .toList();

        Order order = Order.of(userId, orderItems);
        return orderRepository.save(order);
    }
}
