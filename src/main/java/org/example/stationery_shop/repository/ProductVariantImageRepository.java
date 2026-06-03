package org.example.stationery_shop.repository;

import org.example.stationery_shop.entity.catalog.ProductVariantImage;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface ProductVariantImageRepository extends JpaRepository<ProductVariantImage, String> {
    List<ProductVariantImage> findByProductVariantIdOrderBySortOrderAscCreatedAtAsc(String productVariantId);

    Optional<ProductVariantImage> findByProductVariantIdAndSortOrder(String productVariantId, Integer sortOrder);

    @Modifying
    @Query("""
            update ProductVariantImage image
            set image.primaryImage = false
            where image.productVariant.id = :productVariantId
            """)
    void clearPrimaryImage(@Param("productVariantId") String productVariantId);
}
