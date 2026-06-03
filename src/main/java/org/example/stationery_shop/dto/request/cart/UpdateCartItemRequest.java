package org.example.stationery_shop.dto.request.cart;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class UpdateCartItemRequest {
    @NotNull(message = "Quantity khong duoc rong")
    @Min(value = 1, message = "Quantity phai lon hon 0")
    private Integer quantity;
}
