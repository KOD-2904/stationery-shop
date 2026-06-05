package org.example.stationery_shop.dto.request.order;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;
import org.example.stationery_shop.enums.OrderStatus;

@Data
public class UpdateOrderStatusRequest {
    @NotNull(message = "Trang thai don hang khong duoc rong")
    private OrderStatus status;

    @Size(max = 500, message = "Ghi chu khong duoc vuot qua 500 ky tu")
    private String note;
}
