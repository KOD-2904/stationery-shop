package org.example.stationery_shop.repository;

import org.example.stationery_shop.entity.order.Order;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface OrderRepository extends JpaRepository<Order, String> {
    @EntityGraph(attributePaths = {"items", "items.productVariant", "items.productVariant.product", "address", "store", "shippingFeeSnapshot"})
    Optional<Order> findWithItemsById(String id);

    @EntityGraph(attributePaths = {"items", "items.productVariant", "items.productVariant.product", "address", "store", "shippingFeeSnapshot"})
    Optional<Order> findWithItemsByIdAndUserId(String id, String userId);

    @EntityGraph(attributePaths = {"items", "items.productVariant", "items.productVariant.product", "address", "store", "shippingFeeSnapshot"})
    List<Order> findByUserIdOrderByCreatedAtDesc(String userId);
}
