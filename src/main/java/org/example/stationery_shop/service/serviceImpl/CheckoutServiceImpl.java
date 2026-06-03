package org.example.stationery_shop.service.serviceImpl;

import lombok.RequiredArgsConstructor;
import org.example.stationery_shop.dto.request.checkout.CartCheckoutRequest;
import org.example.stationery_shop.dto.request.checkout.CartShippingFeeRequest;
import org.example.stationery_shop.dto.request.checkout.CheckoutItemRequest;
import org.example.stationery_shop.dto.request.checkout.CheckoutRequest;
import org.example.stationery_shop.dto.request.checkout.ShippingFeeRequest;
import org.example.stationery_shop.dto.response.checkout.CheckoutResponse;
import org.example.stationery_shop.dto.response.checkout.ShippingFeeResponse;
import org.example.stationery_shop.dto.response.cart.UnavailableCartItemResponse;
import org.example.stationery_shop.entity.auth.User;
import org.example.stationery_shop.entity.cart.CartItem;
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
import org.example.stationery_shop.repository.AddressRepository;
import org.example.stationery_shop.repository.CartItemRepository;
import org.example.stationery_shop.repository.InventoryRepository;
import org.example.stationery_shop.repository.InventoryReservationRepository;
import org.example.stationery_shop.repository.InventoryTransactionRepository;
import org.example.stationery_shop.repository.OrderRepository;
import org.example.stationery_shop.repository.OrderStatusHistoryRepository;
import org.example.stationery_shop.repository.PaymentRepository;
import org.example.stationery_shop.repository.ProductVariantRepository;
import org.example.stationery_shop.repository.ShipmentRepository;
import org.example.stationery_shop.repository.ShippingFeeSnapshotRepository;
import org.example.stationery_shop.repository.StoreRepository;
import org.example.stationery_shop.service.CheckoutService;
import org.example.stationery_shop.service.CurrentUserService;
import org.example.stationery_shop.service.payment.VnPayService;
import org.example.stationery_shop.service.shipping.GhnClient;
import org.example.stationery_shop.service.shipping.GhnCreateOrderResult;
import org.example.stationery_shop.service.shipping.GhnFeeResult;
import org.example.stationery_shop.service.shipping.GhnProperties;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

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
    private final GhnProperties ghnProperties;
    private final ShipmentRepository shipmentRepository;
    private final CartItemRepository cartItemRepository;


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
        BigDecimal insuranceValue = calculateInsuranceValue(request.getItems());
        Integer serviceId = request.getServiceId();
        Integer serviceTypeId = request.getServiceTypeId();
        if (request.getDeliveryMethod() == DeliveryMethod.SHIP_TO_HOME) {
            Store store = resolveStoreFromItems(request.getItems());
            if (request.getAddressId() == null || request.getAddressId().isBlank()) {
                throw new AppException(ErrorCode.ADDRESS_REQUIRED);
            }
            address = addressRepository.findByIdAndUserId(request.getAddressId(), user.getId())
                    .orElseThrow(() -> new AppException(ErrorCode.ADDRESS_NOT_EXIST));
            GhnFeeResult feeResult = ghnClient.calculateFee(store, address, weight, length, width, height,
                    serviceId, serviceTypeId, insuranceValue);
            shippingFee = feeResult.getFee();
            serviceId = feeResult.getServiceId();
            serviceTypeId = feeResult.getServiceTypeId();
        } else {
            insuranceValue = BigDecimal.ZERO;
        }

        ShippingFeeSnapshot snapshot = shippingFeeSnapshotRepository.save(ShippingFeeSnapshot.builder()
                .user(user)
                .deliveryMethod(request.getDeliveryMethod())
                .address(address)
                .provider(ShippingProvider.GHN)
                .shippingFee(shippingFee)
                .insuranceValue(insuranceValue)
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
    public ShippingFeeResponse calculateShippingFeeFromCart(CartShippingFeeRequest request) {
        ShippingFeeRequest shippingFeeRequest = new ShippingFeeRequest();
        shippingFeeRequest.setDeliveryMethod(request.getDeliveryMethod());
        shippingFeeRequest.setAddressId(request.getAddressId());
        shippingFeeRequest.setWeight(request.getWeight());
        shippingFeeRequest.setLength(request.getLength());
        shippingFeeRequest.setWidth(request.getWidth());
        shippingFeeRequest.setHeight(request.getHeight());
        shippingFeeRequest.setServiceId(request.getServiceId());
        shippingFeeRequest.setServiceTypeId(request.getServiceTypeId());
        shippingFeeRequest.setItems(toCheckoutItems(loadSelectedCartItems(request.getCartItemIds())));
        return calculateShippingFee(shippingFeeRequest);
    }

    @Override
    @Transactional
    public CheckoutResponse checkout(CheckoutRequest request, String clientIp) {
        User user = currentUserService.getCurrentUser();
        Address address = validateAddress(user, request);
        ShippingFeeSnapshot snapshot = validateShippingSnapshot(user, request, address);
        Store checkoutStore = resolveStoreFromItems(request.getItems());

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
        for (CheckoutItemRequest itemRequest : request.getItems()) {
            ProductVariant variant = productVariantRepository.findWithProductById(itemRequest.getProductVariantId())
                    .orElseThrow(() -> new AppException(ErrorCode.PRODUCT_VARIANT_NOT_EXIST));
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
        order.setStore(checkoutStore);
        order.setSubtotal(subtotal);
        order.setTotalAmount(subtotal.add(snapshot.getShippingFee()));
        validateCodAmount(request.getPaymentMethod(), order.getTotalAmount());
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
                .method(request.getPaymentMethod())
                .status(PaymentStatus.PENDING)
                .amount(savedOrder.getTotalAmount())
                .idempotencyKey(UUID.randomUUID().toString())
                .build());
        String paymentUrl = null;
        if (request.getPaymentMethod() == PaymentMethod.VNPAY) {
            paymentUrl = vnPayService.createPaymentUrl(savedOrder, clientIp);
        } else {
            updateOrderStatus(savedOrder, OrderStatus.PROCESSING, "COD checkout created");
            Shipment shipment = getOrCreateShipment(savedOrder, request.getDeliveryMethod() == DeliveryMethod.PICKUP_AT_STORE
                    ? ShippingStatus.NOT_REQUIRED
                    : ShippingStatus.PENDING);
            if (request.getDeliveryMethod() == DeliveryMethod.SHIP_TO_HOME) {
                createGhnOrderForCod(savedOrder, shipment);
            }
        }
        return CheckoutResponse.builder()
                .orderId(savedOrder.getId())
                .paymentId(payment.getId())
                .paymentMethod(request.getPaymentMethod())
                .totalAmount(savedOrder.getTotalAmount())
                .paymentUrl(paymentUrl)
                .build();
    }

    @Override
    @Transactional
    public CheckoutResponse checkoutFromCart(CartCheckoutRequest request, String clientIp) {
        List<CartItem> selectedItems = loadSelectedCartItems(request.getCartItemIds());
        List<UnavailableCartItemResponse> unavailableItems = findUnavailableCartItems(selectedItems);
        if (!unavailableItems.isEmpty()) {
            if (!request.isRemoveUnavailableItems()) {
                return CheckoutResponse.builder()
                        .unavailableItems(unavailableItems)
                        .build();
            }
            cartItemRepository.deleteAll(selectedItems.stream()
                    .filter(item -> unavailableItems.stream()
                            .anyMatch(unavailable -> unavailable.getCartItemId().equals(item.getId())))
                    .toList());
            selectedItems = selectedItems.stream()
                    .filter(item -> unavailableItems.stream()
                            .noneMatch(unavailable -> unavailable.getCartItemId().equals(item.getId())))
                    .toList();
            if (selectedItems.isEmpty()) {
                return CheckoutResponse.builder()
                        .unavailableItems(unavailableItems)
                        .build();
            }
        }

        CheckoutRequest checkoutRequest = new CheckoutRequest();
        checkoutRequest.setDeliveryMethod(request.getDeliveryMethod());
        checkoutRequest.setPaymentMethod(request.getPaymentMethod());
        checkoutRequest.setAddressId(request.getAddressId());
        checkoutRequest.setShippingFeeSnapshotId(request.getShippingFeeSnapshotId());
        checkoutRequest.setNote(request.getNote());
        checkoutRequest.setItems(toCheckoutItems(selectedItems));
        CheckoutResponse response = checkout(checkoutRequest, clientIp);
        cartItemRepository.deleteAll(selectedItems);
        return response;
    }

    private void createGhnOrderForCod(Order order, Shipment shipment) {
        if (StringUtils.hasText(shipment.getGhnOrderCode())) {
            return;
        }
        try {
            GhnCreateOrderResult result = ghnClient.createOrder(order, order.getShippingFeeSnapshot(), PaymentMethod.COD);
            BigDecimal difference = result.getTotalFee().subtract(order.getShippingFeeSnapshot().getShippingFee()).abs();
            if (difference.compareTo(ghnProperties.getMaxFeeDifference()) > 0) {
                markShippingManual(order, shipment, "GHN fee difference exceeds configured threshold");
                return;
            }
            shipment.setStatus(ShippingStatus.CREATED);
            shipment.setGhnOrderCode(result.getOrderCode());
            shipment.setShippingFee(result.getTotalFee());
            shipmentRepository.save(shipment);
            updateOrderStatus(order, OrderStatus.SHIPPING, "GHN order created for COD");
        } catch (Exception exception) {
            markShippingManual(order, shipment, exception.getMessage());
        }
    }

    private void markShippingManual(Order order, Shipment shipment, String note) {
        shipment.setStatus(ShippingStatus.NEED_MANUAL_PROCESSING);
        shipment.setNote(note);
        shipmentRepository.save(shipment);
        updateOrderStatus(order, OrderStatus.NEED_MANUAL_PROCESSING, "GHN create order failed: " + note);
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
                    .insuranceValue(BigDecimal.ZERO)
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
        BigDecimal expectedInsurance = calculateInsuranceValue(request.getItems());
        if (insuranceOrZero(snapshot).compareTo(expectedInsurance) != 0) {
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
                .insuranceValue(insuranceOrZero(snapshot))
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

    private BigDecimal calculateInsuranceValue(List<CheckoutItemRequest> items) {
        if (items == null || items.isEmpty()) {
            return BigDecimal.ZERO;
        }
        BigDecimal subtotal = BigDecimal.ZERO;
        for (CheckoutItemRequest item : items) {
            ProductVariant variant = productVariantRepository.findById(item.getProductVariantId())
                    .orElseThrow(() -> new AppException(ErrorCode.PRODUCT_VARIANT_NOT_EXIST));
            subtotal = subtotal.add(variant.getPrice().multiply(BigDecimal.valueOf(item.getQuantity())));
        }
        BigDecimal maxInsuranceValue = ghnProperties.getMaxInsuranceValue() == null
                ? BigDecimal.ZERO
                : ghnProperties.getMaxInsuranceValue();
        return maxInsuranceValue.compareTo(BigDecimal.ZERO) <= 0 ? BigDecimal.ZERO : subtotal.min(maxInsuranceValue);
    }

    private void validateCodAmount(PaymentMethod paymentMethod, BigDecimal totalAmount) {
        if (paymentMethod != PaymentMethod.COD) {
            return;
        }
        BigDecimal maxCodAmount = ghnProperties.getMaxCodAmount();
        if (maxCodAmount != null
                && maxCodAmount.compareTo(BigDecimal.ZERO) > 0
                && totalAmount.compareTo(maxCodAmount) > 0) {
            throw new AppException(ErrorCode.COD_NOT_ALLOWED_HIGH_VALUE);
        }
    }

    private Store resolveStoreFromItems(List<CheckoutItemRequest> items) {
        if (items == null || items.isEmpty()) {
            throw new AppException(ErrorCode.VALIDATION_ERROR, "Checkout items are required");
        }
        String storeId = items.get(0).getStoreId();
        if (storeId == null || storeId.isBlank()) {
            throw new AppException(ErrorCode.STORE_NOT_EXIST);
        }
        for (CheckoutItemRequest item : items) {
            if (!storeId.equals(item.getStoreId())) {
                throw new AppException(ErrorCode.VALIDATION_ERROR, "Checkout currently supports one store per order");
            }
        }
        return storeRepository.findById(storeId)
                .orElseThrow(() -> new AppException(ErrorCode.STORE_NOT_EXIST));
    }

    private List<CartItem> loadSelectedCartItems(List<String> cartItemIds) {
        User user = currentUserService.getCurrentUser();
        List<CartItem> items = cartItemRepository.findByCartUserIdAndIdIn(user.getId(), cartItemIds);
        if (items.size() != cartItemIds.size()) {
            throw new AppException(ErrorCode.CART_ITEM_NOT_EXIST);
        }
        return items;
    }

    private List<CheckoutItemRequest> toCheckoutItems(List<CartItem> cartItems) {
        return cartItems.stream()
                .map(this::toCheckoutItem)
                .toList();
    }

    private CheckoutItemRequest toCheckoutItem(CartItem cartItem) {
        CheckoutItemRequest item = new CheckoutItemRequest();
        item.setProductVariantId(cartItem.getProductVariant().getId());
        item.setStoreId(cartItem.getStore().getId());
        item.setQuantity(cartItem.getQuantity());
        return item;
    }

    private List<UnavailableCartItemResponse> findUnavailableCartItems(List<CartItem> cartItems) {
        return cartItems.stream()
                .map(this::toUnavailableIfNeeded)
                .filter(item -> item != null)
                .toList();
    }

    private UnavailableCartItemResponse toUnavailableIfNeeded(CartItem item) {
        ProductVariant variant = item.getProductVariant();
        int available = inventoryRepository.findByProductVariantIdAndStoreId(variant.getId(), item.getStore().getId())
                .map(Inventory::getQuantityAvailable)
                .orElse(0);
        if (variant.isActive() && available >= item.getQuantity()) {
            return null;
        }
        return UnavailableCartItemResponse.builder()
                .cartItemId(item.getId())
                .productVariantId(variant.getId())
                .sku(variant.getSku())
                .productName(variant.getProduct().getName())
                .requestedQuantity(item.getQuantity())
                .availableQuantity(available)
                .build();
    }

    private BigDecimal insuranceOrZero(ShippingFeeSnapshot snapshot) {
        return snapshot.getInsuranceValue() == null ? BigDecimal.ZERO : snapshot.getInsuranceValue();
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
