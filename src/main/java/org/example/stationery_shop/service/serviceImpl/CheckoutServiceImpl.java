package org.example.stationery_shop.service.serviceImpl;

import lombok.RequiredArgsConstructor;
import org.example.stationery_shop.dto.request.checkout.CheckoutItemRequest;
import org.example.stationery_shop.dto.request.checkout.CheckoutRequest;
import org.example.stationery_shop.dto.request.checkout.ShippingFeeRequest;
import org.example.stationery_shop.dto.response.checkout.CheckoutResponse;
import org.example.stationery_shop.dto.response.checkout.ShippingFeeResponse;
import org.example.stationery_shop.entity.auth.User;
import org.example.stationery_shop.entity.catalog.ProductVariant;
import org.example.stationery_shop.entity.checkout.ShippingFeeSnapshot;
import org.example.stationery_shop.entity.customer.Address;
import org.example.stationery_shop.entity.inventory.Inventory;
import org.example.stationery_shop.entity.inventory.InventoryReservation;
import org.example.stationery_shop.entity.inventory.InventoryTransaction;
import org.example.stationery_shop.entity.inventory.Store;
import org.example.stationery_shop.entity.order.Order;
import org.example.stationery_shop.entity.order.OrderItem;
import org.example.stationery_shop.entity.order.OrderStatusHistory;
import org.example.stationery_shop.entity.payment.Payment;
import org.example.stationery_shop.enums.DeliveryMethod;
import org.example.stationery_shop.enums.InventoryTransactionType;
import org.example.stationery_shop.enums.OrderStatus;
import org.example.stationery_shop.enums.PaymentMethod;
import org.example.stationery_shop.enums.PaymentStatus;
import org.example.stationery_shop.enums.ReservationStatus;
import org.example.stationery_shop.enums.ShippingProvider;
import org.example.stationery_shop.exception.AppException;
import org.example.stationery_shop.exception.ErrorCode;
import org.example.stationery_shop.repository.AddressRepository;
import org.example.stationery_shop.repository.InventoryRepository;
import org.example.stationery_shop.repository.InventoryReservationRepository;
import org.example.stationery_shop.repository.InventoryTransactionRepository;
import org.example.stationery_shop.repository.OrderRepository;
import org.example.stationery_shop.repository.OrderStatusHistoryRepository;
import org.example.stationery_shop.repository.PaymentRepository;
import org.example.stationery_shop.repository.ProductVariantRepository;
import org.example.stationery_shop.repository.ShippingFeeSnapshotRepository;
import org.example.stationery_shop.repository.StoreRepository;
import org.example.stationery_shop.service.CheckoutService;
import org.example.stationery_shop.service.CurrentUserService;
import org.example.stationery_shop.service.payment.VnPayService;
import org.example.stationery_shop.service.shipping.GhnClient;
import org.example.stationery_shop.service.shipping.GhnFeeResult;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class CheckoutServiceImpl implements CheckoutService {
    private static final int DEFAULT_WEIGHT = 300;
    private static final int DEFAULT_LENGTH = 10;
    private static final int DEFAULT_WIDTH = 10;
    private static final int DEFAULT_HEIGHT = 5;
    private static final long SNAPSHOT_TTL_SECONDS = 15 * 60;
    private static final long RESERVATION_TTL_SECONDS = 15 * 60;

    private final CurrentUserService currentUserService;
    private final AddressRepository addressRepository;
    private final ShippingFeeSnapshotRepository shippingFeeSnapshotRepository;
    private final ProductVariantRepository productVariantRepository;
    private final StoreRepository storeRepository;
    private final InventoryRepository inventoryRepository;
    private final InventoryTransactionRepository inventoryTransactionRepository;
    private final InventoryReservationRepository inventoryReservationRepository;
    private final OrderRepository orderRepository;
    private final OrderStatusHistoryRepository orderStatusHistoryRepository;
    private final PaymentRepository paymentRepository;
    private final VnPayService vnPayService;
    private final GhnClient ghnClient;

    @Override
    @Transactional
    public ShippingFeeResponse calculateShippingFee(ShippingFeeRequest request) {
        User user = currentUserService.getCurrentUser();
        int weight = defaultIfNull(request.getWeight(), DEFAULT_WEIGHT);
        int length = defaultIfNull(request.getLength(), DEFAULT_LENGTH);
        int width = defaultIfNull(request.getWidth(), DEFAULT_WIDTH);
        int height = defaultIfNull(request.getHeight(), DEFAULT_HEIGHT);

        Address address = null;
        BigDecimal shippingFee = BigDecimal.ZERO;
        Integer serviceId = request.getServiceId();
        Integer serviceTypeId = request.getServiceTypeId();
        if (request.getDeliveryMethod() == DeliveryMethod.SHIP_TO_HOME) {
            if (request.getAddressId() == null || request.getAddressId().isBlank()) {
                throw new AppException(ErrorCode.ADDRESS_REQUIRED);
            }
            address = addressRepository.findByIdAndUserId(request.getAddressId(), user.getId())
                    .orElseThrow(() -> new AppException(ErrorCode.ADDRESS_NOT_EXIST));
            GhnFeeResult feeResult = ghnClient.calculateFee(address, weight, length, width, height, serviceId, serviceTypeId);
            shippingFee = feeResult.getFee();
            serviceId = feeResult.getServiceId();
            serviceTypeId = feeResult.getServiceTypeId();
        }

        ShippingFeeSnapshot snapshot = shippingFeeSnapshotRepository.save(ShippingFeeSnapshot.builder()
                .user(user)
                .deliveryMethod(request.getDeliveryMethod())
                .address(address)
                .provider(ShippingProvider.GHN)
                .shippingFee(shippingFee)
                .serviceId(serviceId)
                .serviceTypeId(serviceTypeId)
                .weight(weight)
                .length(length)
                .width(width)
                .height(height)
                .expiredAt(Instant.now().plusSeconds(SNAPSHOT_TTL_SECONDS))
                .used(false)
                .build());
        return toShippingFeeResponse(snapshot);
    }

    @Override
    @Transactional
    public CheckoutResponse checkout(CheckoutRequest request, String clientIp) {
        User user = currentUserService.getCurrentUser();
        Address address = validateAddress(user, request);
        ShippingFeeSnapshot snapshot = validateShippingSnapshot(user, request, address);

        Order order = Order.builder()
                .user(user)
                .status(OrderStatus.PENDING_PAYMENT)
                .deliveryMethod(request.getDeliveryMethod())
                .address(address)
                .shippingFeeSnapshot(snapshot)
                .shippingFee(snapshot.getShippingFee())
                .subtotal(BigDecimal.ZERO)
                .totalAmount(BigDecimal.ZERO)
                .receiverName(address == null ? user.getName() : address.getReceiverName())
                .receiverPhone(address == null ? user.getPhone() : address.getReceiverPhone())
                .shippingAddress(address == null ? null : formatAddress(address))
                .note(request.getNote())
                .items(new ArrayList<>())
                .build();

        BigDecimal subtotal = BigDecimal.ZERO;
        Store firstStore = null;
        for (CheckoutItemRequest itemRequest : request.getItems()) {
            ProductVariant variant = productVariantRepository.findWithProductById(itemRequest.getProductVariantId())
                    .orElseThrow(() -> new AppException(ErrorCode.PRODUCT_VARIANT_NOT_EXIST));
            Store store = storeRepository.findById(itemRequest.getStoreId())
                    .orElseThrow(() -> new AppException(ErrorCode.STORE_NOT_EXIST));
            if (firstStore == null) {
                firstStore = store;
            }
            BigDecimal lineTotal = variant.getPrice().multiply(BigDecimal.valueOf(itemRequest.getQuantity()));
            subtotal = subtotal.add(lineTotal);
            OrderItem item = OrderItem.builder()
                    .order(order)
                    .productVariant(variant)
                    .sku(variant.getSku())
                    .productName(variant.getProduct().getName())
                    .variantSize(variant.getSize())
                    .quantity(itemRequest.getQuantity())
                    .unitPrice(variant.getPrice())
                    .lineTotal(lineTotal)
                    .build();
            order.getItems().add(item);
        }
        order.setStore(firstStore);
        order.setSubtotal(subtotal);
        order.setTotalAmount(subtotal.add(snapshot.getShippingFee()));
        Order savedOrder = orderRepository.save(order);
        orderStatusHistoryRepository.save(OrderStatusHistory.builder()
                .order(savedOrder)
                .newStatus(OrderStatus.PENDING_PAYMENT)
                .note("Checkout created")
                .build());

        for (CheckoutItemRequest itemRequest : request.getItems()) {
            lockInventory(savedOrder, itemRequest);
        }
        snapshot.setUsed(true);
        shippingFeeSnapshotRepository.save(snapshot);

        Payment payment = paymentRepository.save(Payment.builder()
                .order(savedOrder)
                .method(PaymentMethod.VNPAY)
                .status(PaymentStatus.PENDING)
                .amount(savedOrder.getTotalAmount())
                .idempotencyKey(UUID.randomUUID().toString())
                .build());
        String paymentUrl = vnPayService.createPaymentUrl(savedOrder, clientIp);
        return CheckoutResponse.builder()
                .orderId(savedOrder.getId())
                .paymentId(payment.getId())
                .totalAmount(savedOrder.getTotalAmount())
                .paymentUrl(paymentUrl)
                .build();
    }

    private void lockInventory(Order order, CheckoutItemRequest itemRequest) {
        Inventory inventory = inventoryRepository.findLockedByProductVariantIdAndStoreId(
                        itemRequest.getProductVariantId(), itemRequest.getStoreId())
                .orElseThrow(() -> new AppException(ErrorCode.INVENTORY_NOT_EXIST));
        int availableBefore = inventory.getQuantityAvailable();
        int lockedBefore = inventory.getQuantityLocked();
        if (availableBefore < itemRequest.getQuantity()) {
            throw new AppException(ErrorCode.NOT_ENOUGH_STOCK,
                    "Khong du ton kho cho SKU " + inventory.getProductVariant().getSku());
        }
        inventory.setQuantityAvailable(availableBefore - itemRequest.getQuantity());
        inventory.setQuantityLocked(lockedBefore + itemRequest.getQuantity());
        inventoryRepository.save(inventory);
        inventoryReservationRepository.save(InventoryReservation.builder()
                .order(order)
                .inventory(inventory)
                .quantity(itemRequest.getQuantity())
                .status(ReservationStatus.ACTIVE)
                .expiredAt(Instant.now().plusSeconds(RESERVATION_TTL_SECONDS))
                .build());
        inventoryTransactionRepository.save(InventoryTransaction.builder()
                .inventory(inventory)
                .type(InventoryTransactionType.LOCK)
                .quantity(itemRequest.getQuantity())
                .availableBefore(availableBefore)
                .availableAfter(inventory.getQuantityAvailable())
                .lockedBefore(lockedBefore)
                .lockedAfter(inventory.getQuantityLocked())
                .note("Checkout reservation for order " + order.getId())
                .build());
    }

    private Address validateAddress(User user, CheckoutRequest request) {
        if (request.getDeliveryMethod() == DeliveryMethod.PICKUP_AT_STORE) {
            return null;
        }
        if (request.getAddressId() == null || request.getAddressId().isBlank()) {
            throw new AppException(ErrorCode.ADDRESS_REQUIRED);
        }
        return addressRepository.findByIdAndUserId(request.getAddressId(), user.getId())
                .orElseThrow(() -> new AppException(ErrorCode.ADDRESS_NOT_EXIST));
    }

    private ShippingFeeSnapshot validateShippingSnapshot(User user, CheckoutRequest request, Address address) {
        if (request.getDeliveryMethod() == DeliveryMethod.PICKUP_AT_STORE) {
            return shippingFeeSnapshotRepository.save(ShippingFeeSnapshot.builder()
                    .user(user)
                    .deliveryMethod(DeliveryMethod.PICKUP_AT_STORE)
                    .provider(ShippingProvider.GHN)
                    .shippingFee(BigDecimal.ZERO)
                    .weight(DEFAULT_WEIGHT)
                    .length(DEFAULT_LENGTH)
                    .width(DEFAULT_WIDTH)
                    .height(DEFAULT_HEIGHT)
                    .expiredAt(Instant.now().plusSeconds(SNAPSHOT_TTL_SECONDS))
                    .used(false)
                    .build());
        }
        if (request.getShippingFeeSnapshotId() == null || request.getShippingFeeSnapshotId().isBlank()) {
            throw new AppException(ErrorCode.SHIPPING_FEE_SNAPSHOT_NOT_EXIST);
        }
        ShippingFeeSnapshot snapshot = shippingFeeSnapshotRepository
                .findByIdAndUserId(request.getShippingFeeSnapshotId(), user.getId())
                .orElseThrow(() -> new AppException(ErrorCode.SHIPPING_FEE_SNAPSHOT_NOT_EXIST));
        if (snapshot.isUsed() || snapshot.getExpiredAt().isBefore(Instant.now())) {
            throw new AppException(ErrorCode.SHIPPING_FEE_SNAPSHOT_EXPIRED);
        }
        if (snapshot.getDeliveryMethod() != request.getDeliveryMethod()
                || snapshot.getAddress() == null
                || !snapshot.getAddress().getId().equals(address.getId())) {
            throw new AppException(ErrorCode.SHIPPING_FEE_SNAPSHOT_MISMATCH);
        }
        return snapshot;
    }

    private ShippingFeeResponse toShippingFeeResponse(ShippingFeeSnapshot snapshot) {
        return ShippingFeeResponse.builder()
                .snapshotId(snapshot.getId())
                .deliveryMethod(snapshot.getDeliveryMethod())
                .addressId(snapshot.getAddress() == null ? null : snapshot.getAddress().getId())
                .provider(snapshot.getProvider())
                .shippingFee(snapshot.getShippingFee())
                .serviceId(snapshot.getServiceId())
                .serviceTypeId(snapshot.getServiceTypeId())
                .weight(snapshot.getWeight())
                .length(snapshot.getLength())
                .width(snapshot.getWidth())
                .height(snapshot.getHeight())
                .expiredAt(snapshot.getExpiredAt())
                .build();
    }

    private String formatAddress(Address address) {
        return address.getDetailAddress() + ", " + address.getWardName() + ", "
                + address.getDistrictName() + ", " + address.getProvinceName();
    }

    private int defaultIfNull(Integer value, int defaultValue) {
        return value == null ? defaultValue : value;
    }
}
