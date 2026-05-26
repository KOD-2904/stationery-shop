package org.example.stationery_shop.repository;

import org.example.stationery_shop.entity.auth.EmailVerifyToken;
import org.example.stationery_shop.entity.auth.User;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.repository.CrudRepository;

import jakarta.persistence.LockModeType;
import java.util.Optional;

public interface MailRepository extends CrudRepository<EmailVerifyToken, String> {
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    Optional<EmailVerifyToken> findByToken(String token);

    void deleteAllByUser(User user);
}
