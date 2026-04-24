package org.example.stationery_shop.repository;

import org.example.stationery_shop.entity.auth.Role;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface RoleRepository extends JpaRepository<Role, String> {
    Optional<Object> findByCode(String code);
}
