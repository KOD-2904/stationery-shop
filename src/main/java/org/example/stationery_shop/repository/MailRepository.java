package org.example.stationery_shop.repository;

import org.example.stationery_shop.entity.auth.EmailVerifyToken;
import org.springframework.data.repository.CrudRepository;

public interface MailRepository extends CrudRepository<EmailVerifyToken, String> {
}
