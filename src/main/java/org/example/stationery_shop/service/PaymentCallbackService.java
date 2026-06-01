package org.example.stationery_shop.service;

import java.util.Map;

public interface PaymentCallbackService {
    String handleVnPayCallback(Map<String, String> params);
}
