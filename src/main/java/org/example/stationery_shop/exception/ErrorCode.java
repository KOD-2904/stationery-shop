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
    USER_NOT_EXIST(10010, "User khong ton tai" , HttpStatus.NOT_FOUND),
    ACCOUNT_LOCKED(10011, "Tai khoan tam khoa, khieu nai de lam ro" , HttpStatus.BAD_REQUEST),
    VALIDATION_ERROR(10012, "Validation error" , HttpStatus.BAD_REQUEST),
    ACCOUNT_NOT_ACTIVE(10013, "Tai khoan chua duoc kich hoat" , HttpStatus.BAD_REQUEST),
    INVALID_CREDENTIALS(10014, "Email hoac mat khau khong dung" , HttpStatus.BAD_REQUEST),
    ACCOUNT_ALREADY_VERIFIED(10015, "Tai khoan da duoc xac thuc" , HttpStatus.BAD_REQUEST),
    CATEGORY_NOT_EXIST(20000, "Category khong ton tai" , HttpStatus.NOT_FOUND),
    BRAND_NOT_EXIST(20001, "Brand khong ton tai" , HttpStatus.NOT_FOUND),
    PRODUCT_NOT_EXIST(20002, "Product khong ton tai" , HttpStatus.NOT_FOUND),
    PRODUCT_VARIANT_NOT_EXIST(20003, "Product variant khong ton tai" , HttpStatus.NOT_FOUND),
    SLUG_EXISTED(20004, "Slug da ton tai" , HttpStatus.BAD_REQUEST),
    SKU_EXISTED(20005, "SKU da ton tai" , HttpStatus.BAD_REQUEST),
    FILE_IS_EMPTY(20006, "File anh khong duoc rong" , HttpStatus.BAD_REQUEST),
    CLOUDINARY_NOT_CONFIGURED(20007, "Cloudinary chua duoc cau hinh" , HttpStatus.INTERNAL_SERVER_ERROR),
    IMAGE_UPLOAD_FAILED(20008, "Upload anh that bai" , HttpStatus.BAD_REQUEST),
    STORE_NOT_EXIST(30000, "Store khong ton tai" , HttpStatus.NOT_FOUND),
    STORE_CODE_EXISTED(30001, "Store code da ton tai" , HttpStatus.BAD_REQUEST),
    INVENTORY_NOT_EXIST(30002, "Inventory khong ton tai" , HttpStatus.NOT_FOUND),
    NOT_ENOUGH_STOCK(30003, "Khong du so luong ton kho" , HttpStatus.BAD_REQUEST),
    NOT_ENOUGH_LOCKED_STOCK(30004, "Khong du so luong dang khoa" , HttpStatus.BAD_REQUEST),
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
