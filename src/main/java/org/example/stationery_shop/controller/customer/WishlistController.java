package org.example.stationery_shop.controller.customer;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.example.stationery_shop.dto.request.wishlist.AddWishlistItemRequest;
import org.example.stationery_shop.dto.response.ApiResponse;
import org.example.stationery_shop.dto.response.wishlist.WishlistItemResponse;
import org.example.stationery_shop.service.WishlistService;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/wishlist")
@PreAuthorize("hasAnyAuthority('ROLE_ADMIN', 'ROLE_STAFF', 'ROLE_USER')")
public class WishlistController {
    private final WishlistService wishlistService;

    @GetMapping
    public ApiResponse<List<WishlistItemResponse>> getMyWishlist() {
        return ApiResponse.<List<WishlistItemResponse>>builder()
                .code(200)
                .message("Success")
                .result(wishlistService.getMyWishlist())
                .build();
    }

    @PostMapping("/items")
    public ApiResponse<WishlistItemResponse> addItem(@Valid @RequestBody AddWishlistItemRequest request) {
        return ApiResponse.<WishlistItemResponse>builder()
                .code(200)
                .message("Added wishlist item successfully")
                .result(wishlistService.addItem(request))
                .build();
    }

    @DeleteMapping("/items/{productId}")
    public ApiResponse<Void> removeItem(@PathVariable String productId) {
        wishlistService.removeItem(productId);
        return ApiResponse.<Void>builder()
                .code(200)
                .message("Removed wishlist item successfully")
                .build();
    }
}
