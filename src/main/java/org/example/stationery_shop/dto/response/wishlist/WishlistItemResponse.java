package org.example.stationery_shop.dto.response.wishlist;

import lombok.Builder;
import lombok.Data;
import org.example.stationery_shop.dto.response.ProductResponse;

import java.time.Instant;

@Data
@Builder
public class WishlistItemResponse {
    private String id;
    private ProductResponse product;
    private Instant createdAt;
}
