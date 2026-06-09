package org.example.stationery_shop.service.serviceImpl;

import lombok.RequiredArgsConstructor;
import org.example.stationery_shop.dto.request.carrier.CarrierRequest;
import org.example.stationery_shop.dto.response.carrier.CarrierResponse;
import org.example.stationery_shop.dto.response.carrier.CarrierShipmentResponse;
import org.example.stationery_shop.entity.auth.User;
import org.example.stationery_shop.entity.order.Order;
import org.example.stationery_shop.entity.order.OrderStatusHistory;
import org.example.stationery_shop.entity.payment.Payment;
import org.example.stationery_shop.entity.shipping.Carrier;
import org.example.stationery_shop.entity.shipping.CarrierAssignment;
import org.example.stationery_shop.entity.shipping.Shipment;
import org.example.stationery_shop.enums.CarrierAssignmentStatus;
import org.example.stationery_shop.enums.OrderStatus;
import org.example.stationery_shop.enums.PaymentMethod;
import org.example.stationery_shop.enums.PaymentStatus;
import org.example.stationery_shop.enums.ShippingStatus;
import org.example.stationery_shop.exception.AppException;
import org.example.stationery_shop.exception.ErrorCode;
import org.example.stationery_shop.repository.CarrierAssignmentRepository;
import org.example.stationery_shop.repository.CarrierRepository;
import org.example.stationery_shop.repository.OrderRepository;
import org.example.stationery_shop.repository.OrderStatusHistoryRepository;
import org.example.stationery_shop.repository.PaymentRepository;
import org.example.stationery_shop.repository.UserRepository;
import org.example.stationery_shop.service.CarrierService;
import org.example.stationery_shop.service.CurrentUserService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;

@Service
@RequiredArgsConstructor
public class CarrierServiceImpl implements CarrierService {
    private final CarrierRepository carrierRepository;
    private final CarrierAssignmentRepository carrierAssignmentRepository;
    private final OrderRepository orderRepository;
    private final OrderStatusHistoryRepository orderStatusHistoryRepository;
    private final PaymentRepository paymentRepository;
    private final UserRepository userRepository;
    private final CurrentUserService currentUserService;

    @Override
    @Transactional(readOnly = true)
    public List<CarrierResponse> getCarriers() {
        return carrierRepository.findAllByOrderByCreatedAtDesc()
                .stream()
                .map(this::toCarrierResponse)
                .toList();
    }

    @Override
    @Transactional
    public CarrierResponse createCarrier(CarrierRequest request) {
        User user = StringUtils.hasText(request.getUserId())
                ? userRepository.findById(request.getUserId()).orElseThrow(() -> new AppException(ErrorCode.USER_NOT_EXIST))
                : null;
        Carrier carrier = Carrier.builder()
                .user(user)
                .name(request.getName().trim())
                .phone(request.getPhone().trim())
                .provinceId(request.getProvinceId())
                .provinceName(request.getProvinceName())
                .districtId(request.getDistrictId())
                .districtName(request.getDistrictName())
                .currentAssignedOrders(0)
                .maxActiveOrders(request.getMaxActiveOrders())
                .active(request.getActive() == null || request.getActive())
                .build();
        return toCarrierResponse(carrierRepository.save(carrier));
    }

    @Override
    @Transactional
    public CarrierResponse updateCarrier(String id, CarrierRequest request) {
        Carrier carrier = carrierRepository.findById(id)
                .orElseThrow(() -> new AppException(ErrorCode.CARRIER_NOT_EXIST));
        User user = StringUtils.hasText(request.getUserId())
                ? userRepository.findById(request.getUserId()).orElseThrow(() -> new AppException(ErrorCode.USER_NOT_EXIST))
                : null;
        carrier.setUser(user);
        applyCarrierRequest(carrier, request, true);
        return toCarrierResponse(carrierRepository.save(carrier));
    }

    @Override
    @Transactional
    public CarrierResponse updateCarrierActive(String id, boolean active) {
        Carrier carrier = carrierRepository.findById(id)
                .orElseThrow(() -> new AppException(ErrorCode.CARRIER_NOT_EXIST));
        carrier.setActive(active);
        return toCarrierResponse(carrierRepository.save(carrier));
    }

    @Override
    @Transactional(readOnly = true)
    public CarrierResponse getMyProfile() {
        User user = currentUserService.getCurrentUser();
        Carrier carrier = carrierRepository.findByUserId(user.getId())
                .orElseThrow(() -> new AppException(ErrorCode.CARRIER_NOT_EXIST));
        return toCarrierResponse(carrier);
    }

    @Override
    @Transactional
    public CarrierResponse updateMyProfile(CarrierRequest request) {
        User user = currentUserService.getCurrentUser();
        Carrier carrier = carrierRepository.findByUserId(user.getId())
                .orElseThrow(() -> new AppException(ErrorCode.CARRIER_NOT_EXIST));
        applyCarrierRequest(carrier, request, false);
        return toCarrierResponse(carrierRepository.save(carrier));
    }

    @Override
    @Transactional
    public List<CarrierShipmentResponse> getMyShipments() {
        User user = currentUserService.getCurrentUser();
        return carrierAssignmentRepository.findByCarrierUserIdOrderByAssignedAtDesc(user.getId())
                .stream()
                .peek(this::normalizeCompletedAssignment)
                .map(this::toShipmentResponse)
                .toList();
    }

    @Override
    @Transactional
    public CarrierShipmentResponse markPickedUp(String assignmentId) {
        User user = currentUserService.getCurrentUser();
        CarrierAssignment assignment = carrierAssignmentRepository.findByIdAndCarrierUserId(assignmentId, user.getId())
                .orElseThrow(() -> new AppException(ErrorCode.CARRIER_ASSIGNMENT_NOT_EXIST));
        Shipment shipment = assignment.getShipment();
        ensureCarrierStep(
                assignment.getStatus() == CarrierAssignmentStatus.ASSIGNED
                        && shipment.getStatus() == ShippingStatus.READY_FOR_PICKUP
                        && shipment.getOrder().getStatus() == OrderStatus.READY_FOR_PICKUP
        );
        assignment.setStatus(CarrierAssignmentStatus.ACCEPTED);
        assignment.setAcceptedAt(Instant.now());
        carrierAssignmentRepository.save(assignment);
        return toShipmentResponse(assignment);
    }

    @Override
    @Transactional
    public CarrierShipmentResponse markDelivered(String assignmentId) {
        User user = currentUserService.getCurrentUser();
        CarrierAssignment assignment = carrierAssignmentRepository.findByIdAndCarrierUserId(assignmentId, user.getId())
                .orElseThrow(() -> new AppException(ErrorCode.CARRIER_ASSIGNMENT_NOT_EXIST));
        Shipment shipment = assignment.getShipment();
        ensureCarrierStep(
                assignment.getStatus() == CarrierAssignmentStatus.ACCEPTED
                        && shipment.getStatus() == ShippingStatus.READY_FOR_PICKUP
                        && shipment.getOrder().getStatus() == OrderStatus.READY_FOR_PICKUP
        );
        shipment.setStatus(ShippingStatus.SHIPPING);
        updateOrderStatus(shipment.getOrder(), OrderStatus.SHIPPING, "Carrier started delivery");
        carrierAssignmentRepository.save(assignment);
        return toShipmentResponse(assignment);
    }

    @Override
    @Transactional
    public CarrierShipmentResponse markCompleted(String assignmentId) {
        User user = currentUserService.getCurrentUser();
        CarrierAssignment assignment = carrierAssignmentRepository.findByIdAndCarrierUserId(assignmentId, user.getId())
                .orElseThrow(() -> new AppException(ErrorCode.CARRIER_ASSIGNMENT_NOT_EXIST));
        Shipment shipment = assignment.getShipment();
        ensureCarrierStep(
                assignment.getStatus() == CarrierAssignmentStatus.ACCEPTED
                        && shipment.getStatus() == ShippingStatus.SHIPPING
                        && shipment.getOrder().getStatus() == OrderStatus.SHIPPING
        );
        assignment.setStatus(CarrierAssignmentStatus.COMPLETED);
        assignment.setCompletedAt(Instant.now());
        shipment.setStatus(ShippingStatus.COMPLETED);
        Carrier carrier = assignment.getCarrier();
        carrier.setCurrentAssignedOrders(Math.max(0, carrier.getCurrentAssignedOrders() - 1));
        carrierRepository.save(carrier);
        updateOrderStatus(shipment.getOrder(), OrderStatus.COMPLETED, "Carrier completed shipment");
        carrierAssignmentRepository.save(assignment);
        return toShipmentResponse(assignment);
    }

    private CarrierResponse toCarrierResponse(Carrier carrier) {
        User user = carrier.getUser();
        return CarrierResponse.builder()
                .id(carrier.getId())
                .userId(user == null ? null : user.getId())
                .userEmail(user == null ? null : user.getEmail())
                .userName(user == null ? null : user.getName())
                .name(carrier.getName())
                .phone(carrier.getPhone())
                .provinceId(carrier.getProvinceId())
                .provinceName(carrier.getProvinceName())
                .districtId(carrier.getDistrictId())
                .districtName(carrier.getDistrictName())
                .currentAssignedOrders((int) carrierAssignmentRepository.countByCarrierIdAndStatusIn(
                        carrier.getId(),
                        List.of(CarrierAssignmentStatus.ASSIGNED, CarrierAssignmentStatus.ACCEPTED)))
                .maxActiveOrders(carrier.getMaxActiveOrders())
                .active(carrier.isActive())
                .build();
    }

    private void applyCarrierRequest(Carrier carrier, CarrierRequest request, boolean allowAdminFields) {
        carrier.setName(request.getName().trim());
        carrier.setPhone(request.getPhone().trim());
        carrier.setProvinceId(request.getProvinceId());
        carrier.setProvinceName(request.getProvinceName());
        carrier.setDistrictId(request.getDistrictId());
        carrier.setDistrictName(request.getDistrictName());
        carrier.setMaxActiveOrders(request.getMaxActiveOrders());
        if (allowAdminFields && request.getActive() != null) {
            carrier.setActive(request.getActive());
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

    private void ensureCarrierStep(boolean valid) {
        if (!valid) {
            throw new AppException(ErrorCode.INVALID_ORDER_STATUS_TRANSITION);
        }
    }

    private void normalizeCompletedAssignment(CarrierAssignment assignment) {
        if (assignment.getStatus() != CarrierAssignmentStatus.COMPLETED) {
            return;
        }
        Shipment shipment = assignment.getShipment();
        if (shipment.getStatus() != ShippingStatus.COMPLETED) {
            shipment.setStatus(ShippingStatus.COMPLETED);
        }
        updateOrderStatus(shipment.getOrder(), OrderStatus.COMPLETED, "Normalize completed carrier shipment");
    }

    private CarrierShipmentResponse toShipmentResponse(CarrierAssignment assignment) {
        Shipment shipment = assignment.getShipment();
        var order = shipment.getOrder();
        Payment payment = paymentRepository.findByOrderId(order.getId()).orElse(null);
        boolean codRequired = payment != null && payment.getMethod() == PaymentMethod.COD && payment.getStatus() != PaymentStatus.SUCCESS;
        BigDecimal collectAmount = codRequired ? payment.getAmount() : BigDecimal.ZERO;
        Carrier carrier = assignment.getCarrier();
        return CarrierShipmentResponse.builder()
                .assignmentId(assignment.getId())
                .assignmentStatus(assignment.getStatus())
                .assignedAt(assignment.getAssignedAt())
                .acceptedAt(assignment.getAcceptedAt())
                .completedAt(assignment.getCompletedAt())
                .assignmentNote(assignment.getNote())
                .carrierId(carrier.getId())
                .carrierName(carrier.getName())
                .carrierPhone(carrier.getPhone())
                .shipmentId(shipment.getId())
                .provider(shipment.getProvider())
                .shippingStatus(shipment.getStatus())
                .ghnOrderCode(shipment.getGhnOrderCode())
                .shipmentShippingFee(shipment.getShippingFee())
                .shipmentNote(shipment.getNote())
                .orderId(order.getId())
                .orderStatus(order.getStatus())
                .deliveryMethod(order.getDeliveryMethod())
                .totalAmount(order.getTotalAmount())
                .orderShippingFee(order.getShippingFee())
                .receiverName(order.getReceiverName())
                .receiverPhone(order.getReceiverPhone())
                .shippingAddress(order.getShippingAddress())
                .orderNote(order.getNote())
                .orderCreatedAt(order.getCreatedAt())
                .paymentMethod(payment == null ? null : payment.getMethod())
                .paymentStatus(payment == null ? null : payment.getStatus())
                .codRequired(codRequired)
                .collectAmount(collectAmount)
                .build();
    }
}
