package org.example.stationery_shop.repository;

import org.example.stationery_shop.entity.catalog.Product;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface ProductRepository extends JpaRepository<Product, String> {
    boolean existsBySlug(String slug);
    Optional<Product> findBySlug(String slug);

    @EntityGraph(attributePaths = {"brand", "category"})
    List<Product> findByActiveTrueOrderByCreatedAtDesc();

    @EntityGraph(attributePaths = {"brand", "category"})
    List<Product> findAllByOrderByCreatedAtDesc();

    @EntityGraph(attributePaths = {"brand", "category"})
    Optional<Product> findWithBrandAndCategoryById(String id);

    long countByActiveTrue();
}
