package com.loopers.interfaces.api.order;

import com.loopers.application.order.OrderAdminSummary;
import com.loopers.application.order.OrderFacade;
import com.loopers.interfaces.api.ApiResponse;
import com.loopers.interfaces.auth.AdminOnly;
import com.loopers.support.page.PageResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RequiredArgsConstructor
@RestController
public class OrderAdminController {

    private final OrderFacade orderFacade;

    @AdminOnly
    @GetMapping("/api-admin/v1/orders")
    public ApiResponse<PageResponse<OrderAdminDto.SummaryResponse>> getList(
            @RequestParam(name = "page", defaultValue = "0") int page,
            @RequestParam(name = "size", defaultValue = "20") int size) {
        PageRequest pageRequest = PageRequest.of(page, size);
        PageResponse<OrderAdminSummary> result = orderFacade.findAllOrders(pageRequest);
        return ApiResponse.success(result.map(OrderAdminDto.SummaryResponse::from));
    }
}
