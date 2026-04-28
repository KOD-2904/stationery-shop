package org.example.stationery_shop.service.auth;

public interface MailService {
    void sendMail(String to, String verifyLink);
    void verifyMail(String token);
}
