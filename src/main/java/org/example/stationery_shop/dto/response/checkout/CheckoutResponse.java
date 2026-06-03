package org.example.stationery_shop.dto.response.checkout;

import lombok.Builder;
import lombok.Data;
import org.example.stationery_shop.dto.response.cart.UnavailableCartItemResponse;
import org.example.stationery_shop.enums.PaymentMethod;

import java.math.BigDecimal;
import java.util.List;

@Data
@Builder
public class CheckoutResponse {
    private String orderId;
    private String paymentId;
    private PaymentMethod paymentMethod;
    private BigDecimal totalAmount;
    private String paymentUrl;
    private List<UnavailableCartItemResponse> unavailableItems;
}
