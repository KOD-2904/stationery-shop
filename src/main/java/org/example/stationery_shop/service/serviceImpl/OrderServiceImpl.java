package org.example.stationery_shop.service.serviceImpl;

import lombok.RequiredArgsConstructor;
import org.example.stationery_shop.dto.response.order.GhnOrderInfoResponse;
import org.example.stationery_shop.dto.response.order.OrderItemResponse;
import org.example.stationery_shop.dto.response.order.OrderResponse;
import org.example.stationery_shop.entity.auth.User;
import org.example.stationery_shop.entity.order.Order;
import org.example.stationery_shop.entity.order.OrderItem;
import org.example.stationery_shop.entity.shipping.Shipment;
import org.example.stationery_shop.exception.AppException;
import org.example.stationery_shop.exception.ErrorCode;
import org.example.stationery_shop.repository.OrderRepository;
import org.example.stationery_shop.repository.ShipmentRepository;
import org.example.stationery_shop.service.CurrentUserService;
import org.example.stationery_shop.service.OrderService;
import org.example.stationery_shop.service.shipping.GhnClient;
import org.example.stationery_shop.service.shipping.GhnOrderInfoResult;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.util.List;

@Service
@RequiredArgsConstructor
public class OrderServiceImpl implements OrderService {
    private final OrderRepository orderRepository;
    private final ShipmentRepository shipmentRepository;
    private final CurrentUserService currentUserService;
    private final GhnClient ghnClient;

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

    @Override
    @Transactional(readOnly = true)
    public GhnOrderInfoResponse getGhnOrderInfo(String orderId) {
        User user = currentUserService.getCurrentUser();
        Order order = orderRepository.findById(orderId)
                .filter(existingOrder -> existingOrder.getUser().getId().equals(user.getId()))
                .orElseThrow(() -> new AppException(ErrorCode.ORDER_NOT_EXIST));
        List<Shipment> shipments = shipmentRepository.findAllByOrderIdOrderByCreatedAtAsc(order.getId());
        Shipment shipment = shipments.stream()
                .filter(existingShipment -> StringUtils.hasText(existingShipment.getGhnOrderCode()))
                .findFirst()
                .or(() -> shipments.stream().findFirst())
                .orElseThrow(() -> new AppException(ErrorCode.SHIPMENT_NOT_EXIST));
        if (!StringUtils.hasText(shipment.getGhnOrderCode())) {
            throw new AppException(ErrorCode.GHN_ORDER_CODE_NOT_EXIST);
        }
        GhnOrderInfoResult ghnInfo = ghnClient.getOrderInfo(shipment.getGhnOrderCode());
        return GhnOrderInfoResponse.builder()
                .orderId(order.getId())
                .shipmentId(shipment.getId())
                .ghnOrderCode(shipment.getGhnOrderCode())
                .localShippingStatus(shipment.getStatus().name())
                .ghnStatus(ghnInfo.getStatus())
                .ghnStatusName(ghnInfo.getStatusName())
                .ghnAction(ghnInfo.getAction())
                .expectedDeliveryTime(ghnInfo.getExpectedDeliveryTime())
                .estimatedFromTime(ghnInfo.getEstimatedFromTime())
                .estimatedToTime(ghnInfo.getEstimatedToTime())
                .pickupTime(ghnInfo.getPickupTime())
                .orderDate(ghnInfo.getOrderDate())
                .updatedDate(ghnInfo.getUpdatedDate())
                .shippingFee(shipment.getShippingFee())
                .ghnTotalFee(ghnInfo.getTotalFee())
                .codAmount(ghnInfo.getCodAmount())
                .fromName(ghnInfo.getFromName())
                .fromPhone(ghnInfo.getFromPhone())
                .fromAddress(ghnInfo.getFromAddress())
                .toName(ghnInfo.getToName())
                .toPhone(ghnInfo.getToPhone())
                .toAddress(ghnInfo.getToAddress())
                .toWardCode(ghnInfo.getToWardCode())
                .toDistrictId(ghnInfo.getToDistrictId())
                .sortCode(ghnInfo.getSortCode())
                .currentWarehouseId(ghnInfo.getCurrentWarehouseId())
                .pickWarehouseId(ghnInfo.getPickWarehouseId())
                .deliverWarehouseId(ghnInfo.getDeliverWarehouseId())
                .nextWarehouseId(ghnInfo.getNextWarehouseId())
                .lastLogStatus(ghnInfo.getLastLogStatus())
                .lastLogUpdatedDate(ghnInfo.getLastLogUpdatedDate())
                .driverName(ghnInfo.getDriverName())
                .driverPhone(ghnInfo.getDriverPhone())
                .tripCode(ghnInfo.getTripCode())
                .build();
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
