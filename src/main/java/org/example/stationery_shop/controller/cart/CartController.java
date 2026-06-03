package org.example.stationery_shop.controller.cart;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.example.stationery_shop.dto.request.cart.AddCartItemRequest;
import org.example.stationery_shop.dto.request.cart.UpdateCartItemRequest;
import org.example.stationery_shop.dto.response.ApiResponse;
import org.example.stationery_shop.dto.response.cart.CartResponse;
import org.example.stationery_shop.service.CartService;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/cart")
@PreAuthorize("hasAnyAuthority('ROLE_ADMIN', 'ROLE_STAFF', 'ROLE_USER')")
public class CartController {
    private final CartService cartService;

    @GetMapping
    public ApiResponse<CartResponse> getMyCart() {
        return ApiResponse.<CartResponse>builder()
                .code(200)
                .message("Success")
                .result(cartService.getMyCart())
                .build();
    }

    @PostMapping("/items")
    public ApiResponse<CartResponse> addItem(@Valid @RequestBody AddCartItemRequest request) {
        return ApiResponse.<CartResponse>builder()
                .code(200)
                .message("Added item to cart successfully")
                .result(cartService.addItem(request))
                .build();
    }

    @PutMapping("/items/{id}")
    public ApiResponse<CartResponse> updateItem(@PathVariable String id,
                                                @Valid @RequestBody UpdateCartItemRequest request) {
        return ApiResponse.<CartResponse>builder()
                .code(200)
                .message("Updated cart item successfully")
                .result(cartService.updateItem(id, request))
                .build();
    }

    @DeleteMapping("/items/{id}")
    public ApiResponse<CartResponse> removeItem(@PathVariable String id) {
        return ApiResponse.<CartResponse>builder()
                .code(200)
                .message("Removed cart item successfully")
                .result(cartService.removeItem(id))
                .build();
    }

    @DeleteMapping
    public ApiResponse<CartResponse> clearMyCart() {
        return ApiResponse.<CartResponse>builder()
                .code(200)
                .message("Cleared cart successfully")
                .result(cartService.clearMyCart())
                .build();
    }
}
