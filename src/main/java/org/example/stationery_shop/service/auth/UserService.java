package org.example.stationery_shop.service.auth;

import org.example.stationery_shop.dto.request.RegisterRequest;
import org.example.stationery_shop.entity.auth.User;

public interface UserService {
    User register(RegisterRequest registerRequest);

    void verifyUser(String token);
    void resendVerifyToken(User user);
    void resendVerifyToken(String email);
}
