package org.example.stationery_shop.repository;

import org.example.stationery_shop.entity.auth.User;
import org.example.stationery_shop.enums.UserStatus;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, String> {
    boolean existsByEmail(String email);

    Optional<User> findByEmail(String email);

    @EntityGraph(attributePaths = {"roles", "roles.permissions", "providers"})
    Optional<User> findWithRolesByEmail(String email);

    @EntityGraph(attributePaths = {"roles", "roles.permissions", "providers"})
    Optional<User> findWithRolesById(String id);

    @EntityGraph(attributePaths = {"roles", "roles.permissions", "providers"})
    List<User> findAllByOrderByCreatedAtDesc();

    @EntityGraph(attributePaths = {"roles", "roles.permissions", "providers"})
    List<User> findByStatusOrderByCreatedAtDesc(UserStatus status);

    long countByStatus(UserStatus status);
}
