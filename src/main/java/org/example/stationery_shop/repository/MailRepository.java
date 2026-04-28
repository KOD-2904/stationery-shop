package org.example.stationery_shop.repository;

import org.example.stationery_shop.entity.auth.EmailVerifyToken;
import org.example.stationery_shop.entity.auth.User;
import org.springframework.data.repository.CrudRepository;

import java.util.Optional;

public interface MailRepository extends CrudRepository<EmailVerifyToken, String> {
    Optional<EmailVerifyToken> findByToken(String token);

    void deleteAllByUser(User user);
}
