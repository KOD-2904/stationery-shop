package org.example.stationery_shop.service;

import org.example.stationery_shop.dto.request.wishlist.AddWishlistItemRequest;
import org.example.stationery_shop.dto.response.wishlist.WishlistItemResponse;

import java.util.List;

public interface WishlistService {
    List<WishlistItemResponse> getMyWishlist();
    WishlistItemResponse addItem(AddWishlistItemRequest request);
    void removeItem(String productId);
}
