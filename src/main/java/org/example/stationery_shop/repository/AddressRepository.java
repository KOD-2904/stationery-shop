package org.example.stationery_shop.repository;

import org.example.stationery_shop.entity.customer.Address;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.Optional;

public interface AddressRepository extends JpaRepository<Address, String> {
    List<Address> findByUserIdOrderByDefaultAddressDescCreatedAtDesc(String userId);

    Optional<Address> findByIdAndUserId(String id, String userId);

    boolean existsByUserId(String userId);

    @Modifying
    @Query("update Address a set a.defaultAddress = false where a.user.id = :userId and a.id <> :exceptId")
    void clearDefaultAddress(String userId, String exceptId);
}
