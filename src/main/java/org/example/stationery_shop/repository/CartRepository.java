package org.example.stationery_shop.repository;

import org.example.stationery_shop.entity.cart.Cart;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface CartRepository extends JpaRepository<Cart, String> {
    Optional<Cart> findByUserId(String userId);

    @EntityGraph(attributePaths = {
            "items",
            "items.productVariant",
            "items.productVariant.product",
            "items.store"
    })
    Optional<Cart> findWithItemsByUserId(String userId);
}
