package org.example.stationery_shop.service.serviceImpl;

import lombok.RequiredArgsConstructor;
import org.example.stationery_shop.dto.request.wishlist.AddWishlistItemRequest;
import org.example.stationery_shop.dto.response.wishlist.WishlistItemResponse;
import org.example.stationery_shop.entity.auth.User;
import org.example.stationery_shop.entity.catalog.Product;
import org.example.stationery_shop.entity.wishlist.WishlistItem;
import org.example.stationery_shop.exception.AppException;
import org.example.stationery_shop.exception.ErrorCode;
import org.example.stationery_shop.mapper.CatalogMapper;
import org.example.stationery_shop.repository.ProductRepository;
import org.example.stationery_shop.repository.WishlistItemRepository;
import org.example.stationery_shop.service.CurrentUserService;
import org.example.stationery_shop.service.WishlistService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class WishlistServiceImpl implements WishlistService {
    private final CurrentUserService currentUserService;
    private final ProductRepository productRepository;
    private final WishlistItemRepository wishlistItemRepository;
    private final CatalogMapper catalogMapper;

    @Override
    @Transactional(readOnly = true)
    public List<WishlistItemResponse> getMyWishlist() {
        User user = currentUserService.getCurrentUser();
        return wishlistItemRepository.findByUserIdOrderByCreatedAtDesc(user.getId())
                .stream()
                .map(this::toResponse)
                .toList();
    }

    @Override
    @Transactional
    public WishlistItemResponse addItem(AddWishlistItemRequest request) {
        User user = currentUserService.getCurrentUser();
        Product product = productRepository.findWithBrandAndCategoryById(request.getProductId())
                .orElseThrow(() -> new AppException(ErrorCode.PRODUCT_NOT_EXIST));
        WishlistItem item = wishlistItemRepository.findByUserIdAndProductId(user.getId(), product.getId())
                .orElseGet(() -> wishlistItemRepository.save(WishlistItem.builder()
                        .user(user)
                        .product(product)
                        .build()));
        return toResponse(item);
    }

    @Override
    @Transactional
    public void removeItem(String productId) {
        User user = currentUserService.getCurrentUser();
        WishlistItem item = wishlistItemRepository.findByUserIdAndProductId(user.getId(), productId)
                .orElseThrow(() -> new AppException(ErrorCode.WISHLIST_ITEM_NOT_EXIST));
        wishlistItemRepository.delete(item);
    }

    private WishlistItemResponse toResponse(WishlistItem item) {
        return WishlistItemResponse.builder()
                .id(item.getId())
                .product(catalogMapper.toProductResponse(item.getProduct()))
                .createdAt(item.getCreatedAt())
                .build();
    }
}
