package org.example.stationery_shop.repository;

import org.example.stationery_shop.entity.shipping.Carrier;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.Optional;

public interface CarrierRepository extends JpaRepository<Carrier, String> {
    boolean existsByPhoneAndProvinceId(String phone, Integer provinceId);

    Optional<Carrier> findByPhoneAndProvinceId(String phone, Integer provinceId);

    Optional<Carrier> findByUserId(String userId);

    @EntityGraph(attributePaths = {"user"})
    List<Carrier> findAllByOrderByCreatedAtDesc();

    @Query("""
            select c
            from Carrier c
            where c.active = true
              and (:provinceId is null or c.provinceId = :provinceId)
              and c.currentAssignedOrders < c.maxActiveOrders
            order by c.currentAssignedOrders asc, c.createdAt asc
            """)
    List<Carrier> findAssignableCarriers(Integer provinceId);
}
