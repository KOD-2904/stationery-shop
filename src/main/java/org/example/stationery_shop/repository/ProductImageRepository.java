package org.example.stationery_shop.repository;

import org.example.stationery_shop.entity.catalog.ProductImage;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ProductImageRepository extends JpaRepository<ProductImage, String> {
    List<ProductImage> findByProductIdOrderBySortOrderAscCreatedAtAsc(String productId);
    boolean existsByProductIdAndImageUrl(String productId, String imageUrl);
}
