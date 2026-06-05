package org.example.stationery_shop.service.serviceImpl;

import lombok.RequiredArgsConstructor;
import org.example.stationery_shop.dto.request.order.UpdateOrderStatusRequest;
import org.example.stationery_shop.dto.response.order.GhnOrderInfoResponse;
import org.example.stationery_shop.dto.response.order.OrderItemResponse;
import org.example.stationery_shop.dto.response.order.OrderResponse;
import org.example.stationery_shop.dto.response.order.OrderStatusHistoryResponse;
import org.example.stationery_shop.entity.auth.User;
import org.example.stationery_shop.entity.inventory.Inventory;
import org.example.stationery_shop.entity.inventory.InventoryReservation;
import org.example.stationery_shop.entity.inventory.InventoryTransaction;
import org.example.stationery_shop.entity.order.Order;
import org.example.stationery_shop.entity.order.OrderItem;
import org.example.stationery_shop.entity.order.OrderStatusHistory;
import org.example.stationery_shop.entity.shipping.Shipment;
import org.example.stationery_shop.enums.InventoryTransactionType;
import org.example.stationery_shop.enums.OrderStatus;
import org.example.stationery_shop.enums.ReservationStatus;
import org.example.stationery_shop.exception.AppException;
import org.example.stationery_shop.exception.ErrorCode;
import org.example.stationery_shop.repository.InventoryRepository;
import org.example.stationery_shop.repository.InventoryReservationRepository;
import org.example.stationery_shop.repository.InventoryTransactionRepository;
import org.example.stationery_shop.repository.OrderRepository;
import org.example.stationery_shop.repository.OrderStatusHistoryRepository;
import org.example.stationery_shop.repository.ShipmentRepository;
import org.example.stationery_shop.service.CurrentUserService;
import org.example.stationery_shop.service.OrderService;
import org.example.stationery_shop.service.shipping.GhnClient;
import org.example.stationery_shop.service.shipping.GhnOrderInfoResult;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.util.EnumMap;
import java.util.EnumSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

@Service
@RequiredArgsConstructor
public class OrderServiceImpl implements OrderService {
    private static final Map<OrderStatus, Set<OrderStatus>> ALLOWED_TRANSITIONS = buildAllowedTransitions();

    private final OrderRepository orderRepository;
    private final ShipmentRepository shipmentRepository;
    private final OrderStatusHistoryRepository orderStatusHistoryRepository;
    private final InventoryReservationRepository reservationRepository;
    private final InventoryRepository inventoryRepository;
    private final InventoryTransactionRepository inventoryTransactionRepository;
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
    public OrderResponse getOrderForAdmin(String id) {
        Order order = orderRepository.findWithItemsById(id)
                .orElseThrow(() -> new AppException(ErrorCode.ORDER_NOT_EXIST));
        return toResponse(order);
    }

    @Override
    @Transactional(readOnly = true)
    public List<OrderResponse> getOrdersForAdmin(OrderStatus status) {
        List<Order> orders = status == null
                ? orderRepository.findAllByOrderByCreatedAtDesc()
                : orderRepository.findByStatusOrderByCreatedAtDesc(status);
        return orders.stream()
                .map(this::toResponse)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public List<OrderStatusHistoryResponse> getOrderStatusHistory(String orderId) {
        if (!orderRepository.existsById(orderId)) {
            throw new AppException(ErrorCode.ORDER_NOT_EXIST);
        }
        return orderStatusHistoryRepository.findByOrderIdOrderByCreatedAtAsc(orderId)
                .stream()
                .map(this::toStatusHistoryResponse)
                .toList();
    }

    @Override
    @Transactional
    public OrderResponse updateOrderStatus(String orderId, UpdateOrderStatusRequest request) {
        Order order = orderRepository.findWithItemsById(orderId)
                .orElseThrow(() -> new AppException(ErrorCode.ORDER_NOT_EXIST));
        changeStatus(order, request.getStatus(), defaultNote(request.getNote(), "Admin status update"));
        return toResponse(order);
    }

    @Override
    @Transactional
    public OrderResponse cancelMyOrder(String orderId) {
        User user = currentUserService.getCurrentUser();
        Order order = orderRepository.findWithItemsByIdAndUserId(orderId, user.getId())
                .orElseThrow(() -> new AppException(ErrorCode.ORDER_NOT_EXIST));
        changeStatus(order, OrderStatus.CANCELLED, "Customer cancelled order");
        return toResponse(order);
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
                .voucherCode(order.getVoucherCode())
                .discountAmount(order.getDiscountAmount())
                .totalAmount(order.getTotalAmount())
                .receiverName(order.getReceiverName())
                .receiverPhone(order.getReceiverPhone())
                .shippingAddress(order.getShippingAddress())
                .note(order.getNote())
                .createdAt(order.getCreatedAt())
                .items(order.getItems() == null ? List.of() : order.getItems().stream().map(this::toItemResponse).toList())
                .build();
    }

    private void changeStatus(Order order, OrderStatus newStatus, String note) {
        if (order.getStatus() == newStatus) {
            return;
        }
        validateTransition(order, newStatus);
        if (newStatus == OrderStatus.CANCELLED || newStatus == OrderStatus.EXPIRED) {
            releaseActiveReservations(order, note);
        }
        updateOrderStatus(order, newStatus, note);
    }

    private void validateTransition(Order order, OrderStatus newStatus) {
        Set<OrderStatus> allowedStatuses = ALLOWED_TRANSITIONS.getOrDefault(order.getStatus(), Set.of());
        if (!allowedStatuses.contains(newStatus)) {
            throw new AppException(ErrorCode.INVALID_ORDER_STATUS_TRANSITION,
                    "Khong the chuyen don hang tu " + order.getStatus() + " sang " + newStatus);
        }
        if ((newStatus == OrderStatus.CANCELLED || newStatus == OrderStatus.EXPIRED)
                && reservationRepository.existsByOrderIdAndStatus(order.getId(), ReservationStatus.CONFIRMED)) {
            throw new AppException(ErrorCode.INVALID_ORDER_STATUS_TRANSITION,
                    "Don hang da tru ton kho, can xu ly qua return/refund thay vi huy truc tiep");
        }
    }

    private void releaseActiveReservations(Order order, String note) {
        for (InventoryReservation reservation : reservationRepository.findByOrderId(order.getId())) {
            if (reservation.getStatus() != ReservationStatus.ACTIVE) {
                continue;
            }
            Inventory inventory = inventoryRepository.findLockedByProductVariantIdAndStoreId(
                            reservation.getInventory().getProductVariant().getId(),
                            reservation.getInventory().getStore().getId())
                    .orElseThrow(() -> new AppException(ErrorCode.INVENTORY_NOT_EXIST));
            int availableBefore = inventory.getQuantityAvailable();
            int lockedBefore = inventory.getQuantityLocked();
            if (lockedBefore < reservation.getQuantity()) {
                throw new AppException(ErrorCode.NOT_ENOUGH_LOCKED_STOCK);
            }
            inventory.setQuantityAvailable(availableBefore + reservation.getQuantity());
            inventory.setQuantityLocked(lockedBefore - reservation.getQuantity());
            inventoryRepository.save(inventory);
            reservation.setStatus(ReservationStatus.RELEASED);
            reservationRepository.save(reservation);
            inventoryTransactionRepository.save(InventoryTransaction.builder()
                    .inventory(inventory)
                    .type(InventoryTransactionType.RELEASE)
                    .quantity(reservation.getQuantity())
                    .availableBefore(availableBefore)
                    .availableAfter(inventory.getQuantityAvailable())
                    .lockedBefore(lockedBefore)
                    .lockedAfter(inventory.getQuantityLocked())
                    .note(note + " for order " + order.getId())
                    .build());
        }
    }

    private void updateOrderStatus(Order order, OrderStatus newStatus, String note) {
        OrderStatus oldStatus = order.getStatus();
        order.setStatus(newStatus);
        orderRepository.save(order);
        orderStatusHistoryRepository.save(OrderStatusHistory.builder()
                .order(order)
                .oldStatus(oldStatus)
                .newStatus(newStatus)
                .note(note)
                .build());
    }

    private OrderStatusHistoryResponse toStatusHistoryResponse(OrderStatusHistory history) {
        return OrderStatusHistoryResponse.builder()
                .id(history.getId())
                .oldStatus(history.getOldStatus())
                .newStatus(history.getNewStatus())
                .note(history.getNote())
                .createdAt(history.getCreatedAt())
                .build();
    }

    private String defaultNote(String note, String fallback) {
        return StringUtils.hasText(note) ? note : fallback;
    }

    private static Map<OrderStatus, Set<OrderStatus>> buildAllowedTransitions() {
        Map<OrderStatus, Set<OrderStatus>> transitions = new EnumMap<>(OrderStatus.class);
        transitions.put(OrderStatus.PENDING_PAYMENT, EnumSet.of(
                OrderStatus.PAYMENT_FAILED,
                OrderStatus.PROCESSING,
                OrderStatus.READY_FOR_PICKUP,
                OrderStatus.CANCELLED,
                OrderStatus.EXPIRED
        ));
        transitions.put(OrderStatus.PAYMENT_FAILED, EnumSet.of(OrderStatus.CANCELLED));
        transitions.put(OrderStatus.PAID, EnumSet.of(OrderStatus.PROCESSING, OrderStatus.READY_FOR_PICKUP));
        transitions.put(OrderStatus.PROCESSING, EnumSet.of(
                OrderStatus.READY_FOR_PICKUP,
                OrderStatus.SHIPPING,
                OrderStatus.NEED_MANUAL_PROCESSING,
                OrderStatus.CANCELLED
        ));
        transitions.put(OrderStatus.NEED_MANUAL_PROCESSING, EnumSet.of(
                OrderStatus.PROCESSING,
                OrderStatus.READY_FOR_PICKUP,
                OrderStatus.SHIPPING,
                OrderStatus.CANCELLED
        ));
        transitions.put(OrderStatus.READY_FOR_PICKUP, EnumSet.of(OrderStatus.PICKED_UP, OrderStatus.CANCELLED));
        transitions.put(OrderStatus.SHIPPING, EnumSet.of(OrderStatus.DELIVERED, OrderStatus.NEED_MANUAL_PROCESSING));
        transitions.put(OrderStatus.DELIVERED, EnumSet.of(OrderStatus.COMPLETED, OrderStatus.RETURN_REQUESTED));
        transitions.put(OrderStatus.PICKED_UP, EnumSet.of(OrderStatus.COMPLETED, OrderStatus.RETURN_REQUESTED));
        transitions.put(OrderStatus.COMPLETED, EnumSet.of(OrderStatus.RETURN_REQUESTED));
        transitions.put(OrderStatus.RETURN_REQUESTED, EnumSet.of(OrderStatus.RETURNED, OrderStatus.REFUNDED));
        transitions.put(OrderStatus.RETURNED, EnumSet.of(OrderStatus.REFUNDED));
        return transitions;
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
