package org.example.stationery_shop.service;

import org.example.stationery_shop.dto.request.PhoneOtpSendRequest;
import org.example.stationery_shop.dto.request.PhoneOtpVerifyRequest;

public interface PhoneVerificationService {
    void sendOtp(PhoneOtpSendRequest request);
    void verifyOtp(PhoneOtpVerifyRequest request);
}
