package org.example.stationery_shop.service;

import org.example.stationery_shop.dto.response.order.OrderResponse;

import java.util.List;

public interface OrderService {
    OrderResponse getOrder(String id);
    List<OrderResponse> getMyOrders();
}
