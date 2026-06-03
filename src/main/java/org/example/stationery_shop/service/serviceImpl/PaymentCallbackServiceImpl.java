package org.example.stationery_shop.service.serviceImpl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.stationery_shop.entity.checkout.ShippingFeeSnapshot;
import org.example.stationery_shop.entity.inventory.Inventory;
import org.example.stationery_shop.entity.inventory.InventoryReservation;
import org.example.stationery_shop.entity.inventory.InventoryTransaction;
import org.example.stationery_shop.entity.order.Order;
import org.example.stationery_shop.entity.order.OrderStatusHistory;
import org.example.stationery_shop.entity.payment.Payment;
import org.example.stationery_shop.entity.payment.PaymentWebhookLog;
import org.example.stationery_shop.entity.shipping.Shipment;
import org.example.stationery_shop.enums.DeliveryMethod;
import org.example.stationery_shop.enums.InventoryTransactionType;
import org.example.stationery_shop.enums.OrderStatus;
import org.example.stationery_shop.enums.PaymentMethod;
import org.example.stationery_shop.enums.PaymentStatus;
import org.example.stationery_shop.enums.ReservationStatus;
import org.example.stationery_shop.enums.ShippingProvider;
import org.example.stationery_shop.enums.ShippingStatus;
import org.example.stationery_shop.exception.AppException;
import org.example.stationery_shop.exception.ErrorCode;
import org.example.stationery_shop.repository.InventoryRepository;
import org.example.stationery_shop.repository.InventoryReservationRepository;
import org.example.stationery_shop.repository.InventoryTransactionRepository;
import org.example.stationery_shop.repository.OrderRepository;
import org.example.stationery_shop.repository.OrderStatusHistoryRepository;
import org.example.stationery_shop.repository.PaymentRepository;
import org.example.stationery_shop.repository.PaymentWebhookLogRepository;
import org.example.stationery_shop.repository.ShipmentRepository;
import org.example.stationery_shop.service.PaymentCallbackService;
import org.example.stationery_shop.service.payment.VnPayService;
import org.example.stationery_shop.service.shipping.GhnClient;
import org.example.stationery_shop.service.shipping.GhnCreateOrderResult;
import org.example.stationery_shop.service.shipping.GhnProperties;
import org.springframework.stereotype.Service;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.support.TransactionTemplate;
import org.springframework.util.StringUtils;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.StringJoiner;

@Slf4j
@Service
@RequiredArgsConstructor
public class PaymentCallbackServiceImpl implements PaymentCallbackService {
    private static final String VNPAY_SUCCESS_CODE = "00";

    private final VnPayService vnPayService;
    private final PaymentRepository paymentRepository;
    private final PaymentWebhookLogRepository webhookLogRepository;
    private final OrderRepository orderRepository;
    private final OrderStatusHistoryRepository orderStatusHistoryRepository;
    private final InventoryReservationRepository reservationRepository;
    private final InventoryRepository inventoryRepository;
    private final InventoryTransactionRepository inventoryTransactionRepository;
    private final ShipmentRepository shipmentRepository;
    private final GhnClient ghnClient;
    private final GhnProperties ghnProperties;
    private final PlatformTransactionManager transactionManager;

    @Override
    public String handleVnPayCallback(Map<String, String> params) {
        if (!vnPayService.verifySignature(params)) {
            throw new AppException(ErrorCode.INVALID_PAYMENT_SIGNATURE);
        }
        String orderId = params.get("vnp_TxnRef");
        String webhookKey = "VNPAY:" + orderId + ":" + params.getOrDefault("vnp_TransactionNo", "NO_TXN");
        TransactionTemplate transactionTemplate = new TransactionTemplate(transactionManager);
        Order order = transactionTemplate.execute(status -> finalizePayment(params, webhookKey));
        if (order != null && order.getDeliveryMethod() == DeliveryMethod.SHIP_TO_HOME
                && order.getStatus() == OrderStatus.PROCESSING) {
            createGhnOrder(order.getId());
        }
        return "OK";
    }

    private Order finalizePayment(Map<String, String> params, String webhookKey) {
        if (webhookLogRepository.findByIdempotencyKey(webhookKey)
                .filter(PaymentWebhookLog::isProcessed)
                .isPresent()) {
            return null;
        }

        String orderId = params.get("vnp_TxnRef");
        Order order = orderRepository.findWithItemsById(orderId)
                .orElseThrow(() -> new AppException(ErrorCode.ORDER_NOT_EXIST));
        Payment payment = paymentRepository.findByOrderId(orderId)
                .orElseThrow(() -> new AppException(ErrorCode.PAYMENT_NOT_EXIST));

        validateAmount(payment, params.get("vnp_Amount"));
        if (payment.getStatus() == PaymentStatus.SUCCESS) {
            saveWebhookLog(webhookKey, params, true);
            return order;
        }
        if (payment.getStatus() == PaymentStatus.FAILED) {
            saveWebhookLog(webhookKey, params, true);
            return order;
        }

        boolean success = VNPAY_SUCCESS_CODE.equals(params.get("vnp_ResponseCode"))
                && VNPAY_SUCCESS_CODE.equals(params.get("vnp_TransactionStatus"));
        if (success) {
            payment.setStatus(PaymentStatus.SUCCESS);
            payment.setProviderTransactionId(params.get("vnp_TransactionNo"));
            payment.setPaidAt(Instant.now());
            paymentRepository.save(payment);
            confirmReservations(order);
            updateOrderStatus(order, order.getDeliveryMethod() == DeliveryMethod.PICKUP_AT_STORE
                    ? OrderStatus.READY_FOR_PICKUP
                    : OrderStatus.PROCESSING, "VNPAY payment success");
            getOrCreateShipment(order, order.getDeliveryMethod() == DeliveryMethod.PICKUP_AT_STORE
                    ? ShippingStatus.NOT_REQUIRED
                    : ShippingStatus.PENDING);
        } else {
            payment.setStatus(PaymentStatus.FAILED);
            payment.setProviderTransactionId(params.get("vnp_TransactionNo"));
            paymentRepository.save(payment);
            releaseReservations(order);
            updateOrderStatus(order, OrderStatus.PAYMENT_FAILED, "VNPAY payment failed");
        }
        saveWebhookLog(webhookKey, params, true);
        return order;
    }

    private void createGhnOrder(String orderId) {
        TransactionTemplate transactionTemplate = new TransactionTemplate(transactionManager);
        Order order = orderRepository.findWithItemsById(orderId)
                .orElseThrow(() -> new AppException(ErrorCode.ORDER_NOT_EXIST));
        if (shipmentRepository.findAllByOrderIdOrderByCreatedAtAsc(orderId)
                .stream()
                .filter(shipment -> shipment.getGhnOrderCode() != null)
                .findFirst()
                .isPresent()) {
            return;
        }
        try {
            ShippingFeeSnapshot snapshot = order.getShippingFeeSnapshot();
            GhnCreateOrderResult result = ghnClient.createOrder(order, snapshot, PaymentMethod.VNPAY);
            BigDecimal difference = result.getTotalFee().subtract(snapshot.getShippingFee()).abs();
            if (difference.compareTo(ghnProperties.getMaxFeeDifference()) > 0) {
                log.warn("GHN fee difference too high for order {}. snapshot={}, ghn={}",
                        orderId, snapshot.getShippingFee(), result.getTotalFee());
                transactionTemplate.executeWithoutResult(status -> markShippingManual(orderId,
                        "GHN fee difference exceeds configured threshold"));
                return;
            }
            transactionTemplate.executeWithoutResult(status -> saveGhnSuccess(orderId, result));
        } catch (Exception exception) {
            log.error("Create GHN order failed for order {}", orderId, exception);
            transactionTemplate.executeWithoutResult(status -> markShippingManual(orderId, exception.getMessage()));
        }
    }

    private void saveGhnSuccess(String orderId, GhnCreateOrderResult result) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new AppException(ErrorCode.ORDER_NOT_EXIST));
        Shipment shipment = getOrCreateShipment(order, ShippingStatus.PENDING);
        shipment.setStatus(ShippingStatus.CREATED);
        shipment.setGhnOrderCode(result.getOrderCode());
        shipment.setShippingFee(result.getTotalFee());
        shipmentRepository.save(shipment);
        updateOrderStatus(order, OrderStatus.SHIPPING, "GHN order created");
    }

    private void markShippingManual(String orderId, String note) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new AppException(ErrorCode.ORDER_NOT_EXIST));
        Shipment shipment = getOrCreateShipment(order, ShippingStatus.PENDING);
        shipment.setStatus(ShippingStatus.NEED_MANUAL_PROCESSING);
        shipment.setNote(note);
        shipmentRepository.save(shipment);
        updateOrderStatus(order, OrderStatus.NEED_MANUAL_PROCESSING, "GHN create order failed: " + note);
    }

    private void confirmReservations(Order order) {
        for (InventoryReservation reservation : reservationRepository.findByOrderId(order.getId())) {
            if (reservation.getStatus() == ReservationStatus.CONFIRMED) {
                continue;
            }
            if (reservation.getStatus() != ReservationStatus.ACTIVE) {
                throw new AppException(ErrorCode.INVENTORY_RESERVATION_NOT_EXIST);
            }
            Inventory inventory = inventoryRepository.findLockedByProductVariantIdAndStoreId(
                            reservation.getInventory().getProductVariant().getId(), reservation.getInventory().getStore().getId())
                    .orElseThrow(() -> new AppException(ErrorCode.INVENTORY_NOT_EXIST));
            int availableBefore = inventory.getQuantityAvailable();
            int lockedBefore = inventory.getQuantityLocked();
            if (lockedBefore < reservation.getQuantity()) {
                throw new AppException(ErrorCode.NOT_ENOUGH_LOCKED_STOCK);
            }
            inventory.setQuantityLocked(lockedBefore - reservation.getQuantity());
            inventoryRepository.save(inventory);
            reservation.setStatus(ReservationStatus.CONFIRMED);
            reservationRepository.save(reservation);
            inventoryTransactionRepository.save(InventoryTransaction.builder()
                    .inventory(inventory)
                    .type(InventoryTransactionType.DEDUCT)
                    .quantity(reservation.getQuantity())
                    .availableBefore(availableBefore)
                    .availableAfter(inventory.getQuantityAvailable())
                    .lockedBefore(lockedBefore)
                    .lockedAfter(inventory.getQuantityLocked())
                    .note("Payment success for order " + order.getId())
                    .build());
        }
    }

    private void releaseReservations(Order order) {
        for (InventoryReservation reservation : reservationRepository.findByOrderId(order.getId())) {
            if (reservation.getStatus() == ReservationStatus.RELEASED) {
                continue;
            }
            if (reservation.getStatus() != ReservationStatus.ACTIVE) {
                continue;
            }
            Inventory inventory = inventoryRepository.findLockedByProductVariantIdAndStoreId(
                            reservation.getInventory().getProductVariant().getId(), reservation.getInventory().getStore().getId())
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
                    .note("Payment failed for order " + order.getId())
                    .build());
        }
    }

    private void updateOrderStatus(Order order, OrderStatus newStatus, String note) {
        if (order.getStatus() == newStatus) {
            return;
        }
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

    private void validateAmount(Payment payment, String rawAmount) {
        BigDecimal callbackAmount = new BigDecimal(rawAmount).divide(BigDecimal.valueOf(100));
        if (callbackAmount.compareTo(payment.getAmount()) != 0) {
            throw new AppException(ErrorCode.PAYMENT_AMOUNT_MISMATCH);
        }
    }

    private Shipment getOrCreateShipment(Order order, ShippingStatus initialStatus) {
        List<Shipment> shipments = shipmentRepository.findAllByOrderIdOrderByCreatedAtAsc(order.getId());
        if (!shipments.isEmpty()) {
            return shipments.stream()
                    .filter(shipment -> StringUtils.hasText(shipment.getGhnOrderCode()))
                    .findFirst()
                    .orElse(shipments.get(0));
        }
        return shipmentRepository.save(Shipment.builder()
                .order(order)
                .provider(ShippingProvider.GHN)
                .status(initialStatus)
                .shippingFee(order.getShippingFee())
                .build());
    }

    private void saveWebhookLog(String key, Map<String, String> params, boolean processed) {
        webhookLogRepository.findByIdempotencyKey(key).orElseGet(() -> webhookLogRepository.save(PaymentWebhookLog.builder()
                .provider("VNPAY")
                .idempotencyKey(key)
                .rawPayload(toPayload(params))
                .processed(processed)
                .build()));
    }

    private String toPayload(Map<String, String> params) {
        StringJoiner joiner = new StringJoiner("&");
        params.forEach((key, value) -> joiner.add(key + "=" + value));
        return joiner.toString();
    }
}
