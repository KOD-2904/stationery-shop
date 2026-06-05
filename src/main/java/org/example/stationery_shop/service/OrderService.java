package org.example.stationery_shop.service;

import org.example.stationery_shop.dto.request.order.UpdateOrderStatusRequest;
import org.example.stationery_shop.dto.response.order.GhnOrderInfoResponse;
import org.example.stationery_shop.dto.response.order.OrderResponse;
import org.example.stationery_shop.dto.response.order.OrderStatusHistoryResponse;
import org.example.stationery_shop.enums.OrderStatus;

import java.util.List;

public interface OrderService {
    OrderResponse getOrder(String id);
    List<OrderResponse> getMyOrders();
    GhnOrderInfoResponse getGhnOrderInfo(String orderId);
    OrderResponse getOrderForAdmin(String id);
    List<OrderResponse> getOrdersForAdmin(OrderStatus status);
    List<OrderStatusHistoryResponse> getOrderStatusHistory(String orderId);
    OrderResponse updateOrderStatus(String orderId, UpdateOrderStatusRequest request);
    OrderResponse cancelMyOrder(String orderId);
}
