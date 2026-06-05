package org.example.stationery_shop.repository;

import org.example.stationery_shop.entity.voucher.VoucherUsage;
import org.springframework.data.jpa.repository.JpaRepository;

public interface VoucherUsageRepository extends JpaRepository<VoucherUsage, String> {
    boolean existsByOrderId(String orderId);
}
