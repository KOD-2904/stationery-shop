package org.example.stationery_shop.repository;

import org.example.stationery_shop.entity.catalog.ProductVariantImage;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ProductVariantImageRepository extends JpaRepository<ProductVariantImage, String> {
    List<ProductVariantImage> findByProductVariantIdOrderBySortOrderAscCreatedAtAsc(String productVariantId);
}
