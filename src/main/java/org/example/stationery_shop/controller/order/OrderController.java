package org.example.stationery_shop.controller.order;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.example.stationery_shop.dto.request.order.UpdateOrderStatusRequest;
import org.example.stationery_shop.dto.response.ApiResponse;
import org.example.stationery_shop.dto.response.order.GhnOrderInfoResponse;
import org.example.stationery_shop.dto.response.order.OrderResponse;
import org.example.stationery_shop.dto.response.order.OrderStatusHistoryResponse;
import org.example.stationery_shop.enums.OrderStatus;
import org.example.stationery_shop.service.OrderService;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
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

    @PatchMapping("/{id}/cancel")
    public ApiResponse<OrderResponse> cancelMyOrder(@PathVariable String id) {
        return ApiResponse.<OrderResponse>builder()
                .code(200)
                .message("Success")
                .result(orderService.cancelMyOrder(id))
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

    @GetMapping("/admin")
    @PreAuthorize("hasAnyAuthority('ROLE_ADMIN', 'ROLE_STAFF')")
    public ApiResponse<List<OrderResponse>> getOrdersForAdmin(@RequestParam(required = false) OrderStatus status) {
        return ApiResponse.<List<OrderResponse>>builder()
                .code(200)
                .message("Success")
                .result(orderService.getOrdersForAdmin(status))
                .build();
    }

    @GetMapping("/admin/{id}")
    @PreAuthorize("hasAnyAuthority('ROLE_ADMIN', 'ROLE_STAFF')")
    public ApiResponse<OrderResponse> getOrderForAdmin(@PathVariable String id) {
        return ApiResponse.<OrderResponse>builder()
                .code(200)
                .message("Success")
                .result(orderService.getOrderForAdmin(id))
                .build();
    }

    @GetMapping("/admin/{id}/status-history")
    @PreAuthorize("hasAnyAuthority('ROLE_ADMIN', 'ROLE_STAFF')")
    public ApiResponse<List<OrderStatusHistoryResponse>> getOrderStatusHistory(@PathVariable String id) {
        return ApiResponse.<List<OrderStatusHistoryResponse>>builder()
                .code(200)
                .message("Success")
                .result(orderService.getOrderStatusHistory(id))
                .build();
    }

    @PatchMapping("/admin/{id}/status")
    @PreAuthorize("hasAnyAuthority('ROLE_ADMIN', 'ROLE_STAFF')")
    public ApiResponse<OrderResponse> updateOrderStatus(
            @PathVariable String id,
            @Valid @RequestBody UpdateOrderStatusRequest request
    ) {
        return ApiResponse.<OrderResponse>builder()
                .code(200)
                .message("Success")
                .result(orderService.updateOrderStatus(id, request))
                .build();
    }
}
