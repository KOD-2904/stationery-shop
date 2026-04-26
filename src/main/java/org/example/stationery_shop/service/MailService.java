package org.example.stationery_shop.service;

public interface MailService {
    void sendVerifyMail(String to, String verifyLink);
}
