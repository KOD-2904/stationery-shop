package org.example.stationery_shop.dto.response.order;

import lombok.Builder;
import lombok.Data;
import org.example.stationery_shop.enums.DeliveryMethod;
import org.example.stationery_shop.enums.OrderStatus;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;

@Data
@Builder
public class OrderResponse {
    private String id;
    private OrderStatus status;
    private DeliveryMethod deliveryMethod;
    private BigDecimal subtotal;
    private BigDecimal shippingFee;
    private BigDecimal totalAmount;
    private String receiverName;
    private String receiverPhone;
    private String shippingAddress;
    private String note;
    private Instant createdAt;
    private List<OrderItemResponse> items;
}
