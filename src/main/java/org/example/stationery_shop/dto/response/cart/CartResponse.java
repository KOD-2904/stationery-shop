package org.example.stationery_shop.dto.response.cart;

import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.util.List;

@Data
@Builder
public class CartResponse {
    private String id;
    private List<CartItemResponse> items;
    private BigDecimal subtotal;
}
