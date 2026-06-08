package org.example.stationery_shop.dto.response.carrier;

import lombok.Builder;
import lombok.Data;
import org.example.stationery_shop.enums.CarrierAssignmentStatus;
import org.example.stationery_shop.enums.DeliveryMethod;
import org.example.stationery_shop.enums.OrderStatus;
import org.example.stationery_shop.enums.PaymentMethod;
import org.example.stationery_shop.enums.PaymentStatus;
import org.example.stationery_shop.enums.ShippingProvider;
import org.example.stationery_shop.enums.ShippingStatus;

import java.math.BigDecimal;
import java.time.Instant;

@Data
@Builder
public class CarrierShipmentResponse {
    private String assignmentId;
    private CarrierAssignmentStatus assignmentStatus;
    private Instant assignedAt;
    private Instant acceptedAt;
    private Instant completedAt;
    private String assignmentNote;

    private String carrierId;
    private String carrierName;
    private String carrierPhone;

    private String shipmentId;
    private ShippingProvider provider;
    private ShippingStatus shippingStatus;
    private String ghnOrderCode;
    private BigDecimal shipmentShippingFee;
    private String shipmentNote;

    private String orderId;
    private OrderStatus orderStatus;
    private DeliveryMethod deliveryMethod;
    private BigDecimal totalAmount;
    private BigDecimal orderShippingFee;
    private String receiverName;
    private String receiverPhone;
    private String shippingAddress;
    private String orderNote;
    private Instant orderCreatedAt;

    private PaymentMethod paymentMethod;
    private PaymentStatus paymentStatus;
    private boolean codRequired;
    private BigDecimal collectAmount;
}
