package org.example.stationery_shop.dto.response.order;

import lombok.Builder;
import lombok.Data;
import org.example.stationery_shop.enums.OrderStatus;

import java.time.Instant;

@Data
@Builder
public class OrderStatusHistoryResponse {
    private String id;
    private OrderStatus oldStatus;
    private OrderStatus newStatus;
    private String note;
    private Instant createdAt;
}
