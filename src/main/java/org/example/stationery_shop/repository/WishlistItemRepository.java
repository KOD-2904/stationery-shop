package org.example.stationery_shop.repository;

import org.example.stationery_shop.entity.wishlist.WishlistItem;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface WishlistItemRepository extends JpaRepository<WishlistItem, String> {
    @EntityGraph(attributePaths = {"product", "product.brand", "product.category"})
    List<WishlistItem> findByUserIdOrderByCreatedAtDesc(String userId);

    Optional<WishlistItem> findByUserIdAndProductId(String userId, String productId);

    boolean existsByUserIdAndProductId(String userId, String productId);
}
