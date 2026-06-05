package org.example.stationery_shop.repository;

import org.example.stationery_shop.entity.catalog.Category;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface CategoryRepository extends JpaRepository<Category, String> {
    boolean existsBySlug(String slug);
    Optional<Category> findBySlug(String slug);
    List<Category> findByActiveTrueOrderByNameAsc();
    long countByActiveTrue();
}
