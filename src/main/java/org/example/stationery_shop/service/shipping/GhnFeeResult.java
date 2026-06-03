package org.example.stationery_shop.service.shipping;

import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;

@Data
@Builder
public class GhnFeeResult {
    private BigDecimal fee;
    private Integer serviceId;
    private Integer serviceTypeId;
}
