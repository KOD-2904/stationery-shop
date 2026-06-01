package org.example.stationery_shop.dto.response.checkout;

import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;

@Data
@Builder
public class CheckoutResponse {
    private String orderId;
    private String paymentId;
    private BigDecimal totalAmount;
    private String paymentUrl;
}
