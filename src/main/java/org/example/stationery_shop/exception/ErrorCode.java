package org.example.stationery_shop.exception;

import lombok.Getter;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;

@Getter
public enum ErrorCode {
    EMAIL_EXISTED(10000, "Email da ton tai", HttpStatus.BAD_REQUEST),
    ROLE_NOT_EXIST(10001, "Role cha ton tai" , HttpStatus.NOT_FOUND),
    TOKEN_NOT_FOUND(10002, "Token khong ton tai" , HttpStatus.BAD_REQUEST),
    TOKEN_ALREADY_USED(10003, "Token da duoc dung" , HttpStatus.BAD_REQUEST),
    TOKEN_EXPIRED(10004, "Token da het han" , HttpStatus.BAD_REQUEST),
    EMAIL_NOT_EXIST(10005, "Email khong ton tai" , HttpStatus.NOT_FOUND),
    USER_DETAILS_IS_NULL(10006, "User khong ton tai" , HttpStatus.BAD_REQUEST),
    TOKEN_MUST_BE_REFRESH(10007, "Token phai la REFRESH TOKEN" , HttpStatus.BAD_REQUEST),
    TOKEN_MUST_BE_ACCESS(10008, "Token phai la ACCESS TOKEN" , HttpStatus.BAD_REQUEST),
    NOT_VALID_TOKEN(10009, "Token khong hop le" , HttpStatus.BAD_REQUEST),
    ;

    private final int code;
    private final String message;
    private final HttpStatusCode statusCode;

    ErrorCode(int code, String message, HttpStatusCode statusCode) {
        this.code = code;
        this.message = message;
        this.statusCode = statusCode;
    }
}
