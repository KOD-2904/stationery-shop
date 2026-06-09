package org.example.stationery_shop.service;

import org.example.stationery_shop.dto.request.password.ChangePasswordRequest;
import org.example.stationery_shop.dto.request.password.ForgotPasswordOtpRequest;
import org.example.stationery_shop.dto.request.password.ResetPasswordRequest;

public interface PasswordOtpService {
    void sendForgotPasswordOtp(ForgotPasswordOtpRequest request);

    void resetForgotPassword(ResetPasswordRequest request);

    void changePassword(ChangePasswordRequest request);
}
