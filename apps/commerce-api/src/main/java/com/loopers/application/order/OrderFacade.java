package com.loopers.application.order;

import com.loopers.domain.brand.Brand;
import com.loopers.domain.brand.BrandRepository;
import com.loopers.domain.order.*;
import com.loopers.domain.product.Product;
import com.loopers.domain.product.ProductRepository;
import com.loopers.domain.user.User;
import com.loopers.domain.user.UserRepository;
import com.loopers.support.error.CoreException;
import com.loopers.support.error.ErrorType;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
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
    private final StockPolicy stockPolicy;
    private final OrderAssembler orderAssembler;

    @Transactional
    public Long createOrder(Long userId, OrderCommand orderCommand) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new CoreException(ErrorType.NOT_FOUND, "사용자 정보를 찾을 수 없습니다."));

        List<Long> productIds = orderCommand.items().stream().map(OrderCommand.Item::productId).toList();
        List<Product> products = productRepository.findAllByIdIn(productIds);

        Set<Long> foundIds = products.stream().map(Product::getId).collect(Collectors.toSet());
        boolean hasNotFound = productIds.stream().anyMatch(id -> !foundIds.contains(id));
        if (hasNotFound) {
            throw new CoreException(ErrorType.NOT_FOUND, "등록되지 않은 상품입니다.");
        }

        Map<Long, Product> productMap = products.stream()
                .collect(Collectors.toMap(Product::getId, p -> p));

        List<OrderLine> orderLines = orderCommand.items().stream().map(item -> new OrderLine(item.productId(), item.quantity())).toList();
        List<StockPolicy.Shortage> shortages = stockPolicy.findShortages(productMap, orderLines);

        if (!shortages.isEmpty()) {
            String detail = shortages.stream()
                    .map(shortage -> "상품: %s, 요청: %d, 재고: %d".formatted(shortage.productName(), shortage.requested(), shortage.stock()))
                    .collect(Collectors.joining("\n"));
            throw new CoreException(ErrorType.BAD_REQUEST, "재고 부족:\n" + detail);
        }


        List<Long> brandIds = products.stream().map(Product::brandId).toList();
        Map<Long, Brand> brandMap = brandRepository.findAllByIdIn(brandIds).stream()
                .collect(Collectors.toMap(Brand::getId, b -> b));

        // 재고 차감
        orderCommand.items().forEach(item ->
                productMap.get(item.productId()).decreaseStock(item.quantity())
        );

        List<OrderItem> orderItems = orderAssembler.toOrderItems(orderCommand, productMap, brandMap);

        Order order = Order.of(user.getId(), orderItems);
        return orderRepository.save(order);
    }
}
