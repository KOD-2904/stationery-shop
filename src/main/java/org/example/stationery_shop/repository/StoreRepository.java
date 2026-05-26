package org.example.stationery_shop.repository;

import org.example.stationery_shop.entity.inventory.Store;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface StoreRepository extends JpaRepository<Store, String> {
    boolean existsByCode(String code);
    Optional<Store> findByCode(String code);
    List<Store> findByActiveTrueOrderByNameAsc();
}
