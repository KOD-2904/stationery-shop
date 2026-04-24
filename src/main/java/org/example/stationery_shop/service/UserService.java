package org.example.stationery_shop.service;

import org.example.stationery_shop.dto.request.RegisterRequest;
import org.example.stationery_shop.entity.auth.User;

public interface UserService {
    User register(RegisterRequest registerRequest);
}
