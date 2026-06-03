package org.example.stationery_shop.repository;

import org.example.stationery_shop.entity.cart.CartItem;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface CartItemRepository extends JpaRepository<CartItem, String> {
    @EntityGraph(attributePaths = {"cart", "productVariant", "productVariant.product", "store"})
    Optional<CartItem> findByIdAndCartUserId(String id, String userId);

    Optional<CartItem> findByCartIdAndProductVariantIdAndStoreId(String cartId, String productVariantId, String storeId);

    @EntityGraph(attributePaths = {"cart", "productVariant", "productVariant.product", "store"})
    List<CartItem> findByCartUserIdAndIdIn(String userId, List<String> ids);
}
