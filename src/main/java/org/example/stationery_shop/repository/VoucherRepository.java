package org.example.stationery_shop.repository;

import org.example.stationery_shop.entity.voucher.Voucher;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface VoucherRepository extends JpaRepository<Voucher, String> {
    boolean existsByCodeIgnoreCase(String code);

    @EntityGraph(attributePaths = {"categories"})
    Optional<Voucher> findByCodeIgnoreCase(String code);

    @EntityGraph(attributePaths = {"categories"})
    List<Voucher> findAllByOrderByCreatedAtDesc();

    @EntityGraph(attributePaths = {"categories"})
    List<Voucher> findByActiveTrueOrderByCreatedAtDesc();
}
