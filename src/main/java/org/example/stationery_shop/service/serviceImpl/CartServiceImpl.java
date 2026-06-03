package org.example.stationery_shop.service.serviceImpl;

import lombok.RequiredArgsConstructor;
import org.example.stationery_shop.dto.request.cart.AddCartItemRequest;
import org.example.stationery_shop.dto.request.cart.UpdateCartItemRequest;
import org.example.stationery_shop.dto.response.cart.CartItemResponse;
import org.example.stationery_shop.dto.response.cart.CartResponse;
import org.example.stationery_shop.entity.auth.User;
import org.example.stationery_shop.entity.cart.Cart;
import org.example.stationery_shop.entity.cart.CartItem;
import org.example.stationery_shop.entity.catalog.ProductVariant;
import org.example.stationery_shop.entity.inventory.Inventory;
import org.example.stationery_shop.entity.inventory.Store;
import org.example.stationery_shop.exception.AppException;
import org.example.stationery_shop.exception.ErrorCode;
import org.example.stationery_shop.repository.CartItemRepository;
import org.example.stationery_shop.repository.CartRepository;
import org.example.stationery_shop.repository.InventoryRepository;
import org.example.stationery_shop.repository.ProductVariantRepository;
import org.example.stationery_shop.repository.StoreRepository;
import org.example.stationery_shop.service.CartService;
import org.example.stationery_shop.service.CurrentUserService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.ArrayList;

@Service
@RequiredArgsConstructor
public class CartServiceImpl implements CartService {
    private final CurrentUserService currentUserService;
    private final CartRepository cartRepository;
    private final CartItemRepository cartItemRepository;
    private final ProductVariantRepository productVariantRepository;
    private final StoreRepository storeRepository;
    private final InventoryRepository inventoryRepository;

    @Override
    @Transactional
    public CartResponse getMyCart() {
        User user = currentUserService.getCurrentUser();
        return toResponse(getOrCreateCart(user));
    }

    @Override
    @Transactional
    public CartResponse addItem(AddCartItemRequest request) {
        User user = currentUserService.getCurrentUser();
        Cart cart = getOrCreateCart(user);
        ProductVariant variant = productVariantRepository.findWithProductById(request.getProductVariantId())
                .orElseThrow(() -> new AppException(ErrorCode.PRODUCT_VARIANT_NOT_EXIST));
        Store store = storeRepository.findById(request.getStoreId())
                .orElseThrow(() -> new AppException(ErrorCode.STORE_NOT_EXIST));
        CartItem item = cartItemRepository
                .findByCartIdAndProductVariantIdAndStoreId(cart.getId(), variant.getId(), store.getId())
                .orElseGet(() -> CartItem.builder()
                        .cart(cart)
                        .productVariant(variant)
                        .store(store)
                        .quantity(0)
                        .build());
        item.setQuantity(item.getQuantity() + request.getQuantity());
        cartItemRepository.save(item);
        return toResponse(loadCart(user.getId()));
    }

    @Override
    @Transactional
    public CartResponse updateItem(String cartItemId, UpdateCartItemRequest request) {
        User user = currentUserService.getCurrentUser();
        CartItem item = cartItemRepository.findByIdAndCartUserId(cartItemId, user.getId())
                .orElseThrow(() -> new AppException(ErrorCode.CART_ITEM_NOT_EXIST));
        item.setQuantity(request.getQuantity());
        cartItemRepository.save(item);
        return toResponse(loadCart(user.getId()));
    }

    @Override
    @Transactional
    public CartResponse removeItem(String cartItemId) {
        User user = currentUserService.getCurrentUser();
        CartItem item = cartItemRepository.findByIdAndCartUserId(cartItemId, user.getId())
                .orElseThrow(() -> new AppException(ErrorCode.CART_ITEM_NOT_EXIST));
        cartItemRepository.delete(item);
        return toResponse(loadCart(user.getId()));
    }

    @Override
    @Transactional
    public CartResponse clearMyCart() {
        User user = currentUserService.getCurrentUser();
        Cart cart = loadCart(user.getId());
        cart.getItems().clear();
        cartRepository.save(cart);
        return toResponse(cart);
    }

    private Cart getOrCreateCart(User user) {
        return cartRepository.findWithItemsByUserId(user.getId())
                .orElseGet(() -> cartRepository.save(Cart.builder()
                        .user(user)
                        .items(new ArrayList<>())
                        .build()));
    }

    private Cart loadCart(String userId) {
        return cartRepository.findWithItemsByUserId(userId)
                .orElseThrow(() -> new AppException(ErrorCode.CART_EMPTY));
    }

    private CartResponse toResponse(Cart cart) {
        var items = cart.getItems().stream()
                .map(this::toItemResponse)
                .toList();
        BigDecimal subtotal = items.stream()
                .map(CartItemResponse::getLineTotal)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        return CartResponse.builder()
                .id(cart.getId())
                .items(items)
                .subtotal(subtotal)
                .build();
    }

    private CartItemResponse toItemResponse(CartItem item) {
        ProductVariant variant = item.getProductVariant();
        int available = inventoryRepository.findByProductVariantIdAndStoreId(variant.getId(), item.getStore().getId())
                .map(Inventory::getQuantityAvailable)
                .orElse(0);
        BigDecimal lineTotal = variant.getPrice().multiply(BigDecimal.valueOf(item.getQuantity()));
        return CartItemResponse.builder()
                .id(item.getId())
                .productVariantId(variant.getId())
                .sku(variant.getSku())
                .productName(variant.getProduct().getName())
                .variantSize(variant.getSize())
                .storeId(item.getStore().getId())
                .storeCode(item.getStore().getCode())
                .quantity(item.getQuantity())
                .availableQuantity(available)
                .availableForCheckout(variant.isActive() && available >= item.getQuantity())
                .unitPrice(variant.getPrice())
                .lineTotal(lineTotal)
                .build();
    }
}
