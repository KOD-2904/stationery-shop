package org.example.stationery_shop.repository;

import org.example.stationery_shop.entity.catalog.ProductVariant;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface ProductVariantRepository extends JpaRepository<ProductVariant, String> {
    boolean existsBySku(String sku);

    @EntityGraph(attributePaths = {"product", "product.brand", "product.category"})
    Optional<ProductVariant> findWithProductById(String id);

    List<ProductVariant> findByProductIdOrderBySizeAsc(String productId);
}
