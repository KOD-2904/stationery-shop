package org.example.stationery_shop.service.auth;

public interface MailService {
    void sendMail(String to, String verifyLink);
    void sendSimpleMail(String to, String subject, String content);
    void verifyMail(String token);
}
