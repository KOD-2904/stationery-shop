package org.example.stationery_shop.service.serviceImpl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.stationery_shop.entity.inventory.Inventory;
import org.example.stationery_shop.entity.inventory.InventoryReservation;
import org.example.stationery_shop.entity.inventory.InventoryTransaction;
import org.example.stationery_shop.entity.order.Order;
import org.example.stationery_shop.entity.order.OrderStatusHistory;
import org.example.stationery_shop.entity.payment.Payment;
import org.example.stationery_shop.enums.InventoryTransactionType;
import org.example.stationery_shop.enums.OrderStatus;
import org.example.stationery_shop.enums.PaymentStatus;
import org.example.stationery_shop.enums.ReservationStatus;
import org.example.stationery_shop.exception.AppException;
import org.example.stationery_shop.exception.ErrorCode;
import org.example.stationery_shop.repository.InventoryRepository;
import org.example.stationery_shop.repository.InventoryReservationRepository;
import org.example.stationery_shop.repository.InventoryTransactionRepository;
import org.example.stationery_shop.repository.OrderRepository;
import org.example.stationery_shop.repository.OrderStatusHistoryRepository;
import org.example.stationery_shop.repository.PaymentRepository;
import org.example.stationery_shop.service.OrderExpirationService;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.LinkedHashSet;
import java.util.Set;

@Slf4j
@Service
@RequiredArgsConstructor
public class OrderExpirationServiceImpl implements OrderExpirationService {
    private final InventoryReservationRepository reservationRepository;
    private final InventoryRepository inventoryRepository;
    private final InventoryTransactionRepository inventoryTransactionRepository;
    private final OrderRepository orderRepository;
    private final OrderStatusHistoryRepository orderStatusHistoryRepository;
    private final PaymentRepository paymentRepository;

    @Override
    @Scheduled(fixedDelayString = "${checkout.expiration-scan-delay-ms:60000}")
    @Transactional
    public void expirePendingOrders() {
        Set<String> orderIds = new LinkedHashSet<>();
        reservationRepository.findByStatusAndExpiredAtBefore(ReservationStatus.ACTIVE, Instant.now())
                .forEach(reservation -> orderIds.add(reservation.getOrder().getId()));
        for (String orderId : orderIds) {
            expireOrder(orderId);
        }
    }

    private void expireOrder(String orderId) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new AppException(ErrorCode.ORDER_NOT_EXIST));
        if (order.getStatus() != OrderStatus.PENDING_PAYMENT) {
            return;
        }
        Payment payment = paymentRepository.findByOrderId(orderId)
                .orElseThrow(() -> new AppException(ErrorCode.PAYMENT_NOT_EXIST));
        if (payment.getStatus() != PaymentStatus.PENDING) {
            return;
        }

        releaseExpiredReservations(order);
        payment.setStatus(PaymentStatus.FAILED);
        paymentRepository.save(payment);
        updateOrderStatus(order, OrderStatus.EXPIRED, "Payment expired");
        log.info("Expired pending order {} and released locked inventory", orderId);
    }

    private void releaseExpiredReservations(Order order) {
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
            reservation.setStatus(ReservationStatus.EXPIRED);
            reservationRepository.save(reservation);
            inventoryTransactionRepository.save(InventoryTransaction.builder()
                    .inventory(inventory)
                    .type(InventoryTransactionType.RELEASE)
                    .quantity(reservation.getQuantity())
                    .availableBefore(availableBefore)
                    .availableAfter(inventory.getQuantityAvailable())
                    .lockedBefore(lockedBefore)
                    .lockedAfter(inventory.getQuantityLocked())
                    .note("Payment expired for order " + order.getId())
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
}
