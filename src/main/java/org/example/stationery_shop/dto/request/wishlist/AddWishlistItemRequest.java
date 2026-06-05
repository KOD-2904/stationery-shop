package org.example.stationery_shop.dto.request.wishlist;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class AddWishlistItemRequest {
    @NotBlank(message = "Product id khong duoc rong")
    private String productId;
}
