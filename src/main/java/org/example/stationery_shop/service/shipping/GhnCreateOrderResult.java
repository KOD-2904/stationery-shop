package org.example.stationery_shop.service.shipping;

import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;

@Data
@Builder
public class GhnCreateOrderResult {
    private String orderCode;
    private BigDecimal totalFee;
}
