package org.example.stationery_shop.service.serviceImpl;

import lombok.RequiredArgsConstructor;
import org.example.stationery_shop.dto.response.order.OrderItemResponse;
import org.example.stationery_shop.dto.response.order.OrderResponse;
import org.example.stationery_shop.entity.auth.User;
import org.example.stationery_shop.entity.order.Order;
import org.example.stationery_shop.entity.order.OrderItem;
import org.example.stationery_shop.exception.AppException;
import org.example.stationery_shop.exception.ErrorCode;
import org.example.stationery_shop.repository.OrderRepository;
import org.example.stationery_shop.service.CurrentUserService;
import org.example.stationery_shop.service.OrderService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class OrderServiceImpl implements OrderService {
    private final OrderRepository orderRepository;
    private final CurrentUserService currentUserService;

    @Override
    @Transactional(readOnly = true)
    public OrderResponse getOrder(String id) {
        User user = currentUserService.getCurrentUser();
        Order order = orderRepository.findWithItemsByIdAndUserId(id, user.getId())
                .orElseThrow(() -> new AppException(ErrorCode.ORDER_NOT_EXIST));
        return toResponse(order);
    }

    @Override
    @Transactional(readOnly = true)
    public List<OrderResponse> getMyOrders() {
        User user = currentUserService.getCurrentUser();
        return orderRepository.findByUserIdOrderByCreatedAtDesc(user.getId())
                .stream()
                .map(this::toResponse)
                .toList();
    }

    private OrderResponse toResponse(Order order) {
        return OrderResponse.builder()
                .id(order.getId())
                .status(order.getStatus())
                .deliveryMethod(order.getDeliveryMethod())
                .subtotal(order.getSubtotal())
                .shippingFee(order.getShippingFee())
                .totalAmount(order.getTotalAmount())
                .receiverName(order.getReceiverName())
                .receiverPhone(order.getReceiverPhone())
                .shippingAddress(order.getShippingAddress())
                .note(order.getNote())
                .createdAt(order.getCreatedAt())
                .items(order.getItems() == null ? List.of() : order.getItems().stream().map(this::toItemResponse).toList())
                .build();
    }

    private OrderItemResponse toItemResponse(OrderItem item) {
        return OrderItemResponse.builder()
                .productVariantId(item.getProductVariant().getId())
                .sku(item.getSku())
                .productName(item.getProductName())
                .variantSize(item.getVariantSize())
                .quantity(item.getQuantity())
                .unitPrice(item.getUnitPrice())
                .lineTotal(item.getLineTotal())
                .build();
    }
}
