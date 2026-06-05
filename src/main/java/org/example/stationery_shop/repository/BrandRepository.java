package org.example.stationery_shop.repository;

import org.example.stationery_shop.entity.catalog.Brand;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface BrandRepository extends JpaRepository<Brand, String> {
    boolean existsBySlug(String slug);
    Optional<Brand> findBySlug(String slug);
    List<Brand> findByActiveTrueOrderByNameAsc();
    long countByActiveTrue();
}
