package org.example.stationery_shop.service;

import org.example.stationery_shop.dto.request.cart.AddCartItemRequest;
import org.example.stationery_shop.dto.request.cart.UpdateCartItemRequest;
import org.example.stationery_shop.dto.response.cart.CartResponse;

public interface CartService {
    CartResponse getMyCart();
    CartResponse addItem(AddCartItemRequest request);
    CartResponse updateItem(String cartItemId, UpdateCartItemRequest request);
    CartResponse removeItem(String cartItemId);
    CartResponse clearMyCart();
}
