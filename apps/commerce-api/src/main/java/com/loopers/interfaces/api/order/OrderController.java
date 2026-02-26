package com.loopers.interfaces.api.order;

import com.loopers.application.order.OrderCommand;
import com.loopers.application.order.OrderFacade;
import com.loopers.interfaces.api.ApiResponse;
import com.loopers.interfaces.auth.AuthenticatedUser;
import com.loopers.interfaces.auth.CurrentUser;
import com.loopers.interfaces.auth.LoginRequired;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RequiredArgsConstructor
@RestController
public class OrderController {

    private final OrderFacade orderFacade;

    @LoginRequired
    @ResponseStatus(HttpStatus.CREATED)
    @PostMapping("/api/v1/orders")
    public ApiResponse<OrderDto.CreateOrderResponse> createOrder(@CurrentUser AuthenticatedUser user,
                                                                 @RequestBody OrderDto.CreateOrderRequest request) {

        OrderCommand orderCommand = new OrderCommand(
                request.items().stream()
                        .map(item -> new OrderCommand.Item(item.productId(), item.quantity()))
                        .toList()
        );

        Long orderId = orderFacade.createOrder(user.id(), orderCommand);
        OrderDto.CreateOrderResponse response = new OrderDto.CreateOrderResponse(orderId);
        return ApiResponse.success(response);
    }

}
