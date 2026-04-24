package org.example.stationery_shop.exception;

import lombok.Getter;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;

@Getter
public enum ErrorCode {
    EMAIL_EXISTED(10000, "Email da ton tai", HttpStatus.BAD_REQUEST),
    ROLE_NOT_EXIST(10001, "Role cha ton tai" , HttpStatus.NOT_FOUND),;

    private final int code;
    private final String message;
    private final HttpStatusCode statusCode;

    ErrorCode(int code, String message, HttpStatusCode statusCode) {
        this.code = code;
        this.message = message;
        this.statusCode = statusCode;
    }
}
