package org.example.stationery_shop.controller.order;

import lombok.RequiredArgsConstructor;
import org.example.stationery_shop.dto.response.ApiResponse;
import org.example.stationery_shop.dto.response.order.GhnOrderInfoResponse;
import org.example.stationery_shop.dto.response.order.OrderResponse;
import org.example.stationery_shop.service.OrderService;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/orders")
@PreAuthorize("hasAnyAuthority('ROLE_ADMIN', 'ROLE_STAFF', 'ROLE_USER')")
public class OrderController {
    private final OrderService orderService;

    @GetMapping("/{id}")
    public ApiResponse<OrderResponse> getOrder(@PathVariable String id) {
        return ApiResponse.<OrderResponse>builder()
                .code(200)
                .message("Success")
                .result(orderService.getOrder(id))
                .build();
    }

    @GetMapping("/my-orders")
    public ApiResponse<List<OrderResponse>> getMyOrders() {
        return ApiResponse.<List<OrderResponse>>builder()
                .code(200)
                .message("Success")
                .result(orderService.getMyOrders())
                .build();
    }

    @GetMapping("/{id}/ghn-info")
    public ApiResponse<GhnOrderInfoResponse> getGhnOrderInfo(@PathVariable String id) {
        return ApiResponse.<GhnOrderInfoResponse>builder()
                .code(200)
                .message("Success")
                .result(orderService.getGhnOrderInfo(id))
                .build();
    }
}
