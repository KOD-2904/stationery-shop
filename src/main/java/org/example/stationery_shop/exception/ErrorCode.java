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
    CATALOG_IMPORT_FILE_INVALID(20009, "File import catalog khong hop le" , HttpStatus.BAD_REQUEST),
    IMAGE_IMPORT_FOLDER_INVALID(20010, "Thu muc import anh khong hop le" , HttpStatus.BAD_REQUEST),
    IMAGE_IMPORT_FOLDER_EMPTY(20011, "Thu muc import anh khong co file hop le" , HttpStatus.BAD_REQUEST),
    IMAGE_IMPORT_JOB_NOT_FOUND(20012, "Image import job khong ton tai" , HttpStatus.NOT_FOUND),
    STORE_NOT_EXIST(30000, "Store khong ton tai" , HttpStatus.NOT_FOUND),
    STORE_CODE_EXISTED(30001, "Store code da ton tai" , HttpStatus.BAD_REQUEST),
    INVENTORY_NOT_EXIST(30002, "Inventory khong ton tai" , HttpStatus.NOT_FOUND),
    NOT_ENOUGH_STOCK(30003, "Khong du so luong ton kho" , HttpStatus.BAD_REQUEST),
    NOT_ENOUGH_LOCKED_STOCK(30004, "Khong du so luong dang khoa" , HttpStatus.BAD_REQUEST),
    ADDRESS_NOT_EXIST(40000, "Address khong ton tai" , HttpStatus.NOT_FOUND),
    ADDRESS_REQUIRED(40001, "Can chon dia chi giao hang" , HttpStatus.BAD_REQUEST),
    SHIPPING_FEE_SNAPSHOT_NOT_EXIST(40002, "Can tinh phi ship truoc khi checkout" , HttpStatus.BAD_REQUEST),
    SHIPPING_FEE_SNAPSHOT_EXPIRED(40003, "Phi ship da het han, vui long tinh lai" , HttpStatus.BAD_REQUEST),
    SHIPPING_FEE_SNAPSHOT_MISMATCH(40004, "Phi ship khong khop voi thong tin checkout" , HttpStatus.BAD_REQUEST),
    CART_ITEM_NOT_EXIST(40005, "Cart item khong ton tai" , HttpStatus.NOT_FOUND),
    CART_EMPTY(40006, "Gio hang dang trong" , HttpStatus.BAD_REQUEST),
    ORDER_NOT_EXIST(50000, "Order khong ton tai" , HttpStatus.NOT_FOUND),
    PAYMENT_NOT_EXIST(50001, "Payment khong ton tai" , HttpStatus.NOT_FOUND),
    INVALID_PAYMENT_SIGNATURE(50002, "Chu ky thanh toan khong hop le" , HttpStatus.BAD_REQUEST),
    PAYMENT_AMOUNT_MISMATCH(50003, "So tien thanh toan khong khop" , HttpStatus.BAD_REQUEST),
    PAYMENT_ALREADY_FINALIZED(50004, "Payment da duoc xu ly" , HttpStatus.BAD_REQUEST),
    INVENTORY_RESERVATION_NOT_EXIST(50005, "Reservation khong ton tai" , HttpStatus.NOT_FOUND),
    GHN_NOT_CONFIGURED(60000, "GHN chua duoc cau hinh" , HttpStatus.INTERNAL_SERVER_ERROR),
    GHN_CREATE_ORDER_FAILED(60001, "Tao don GHN that bai" , HttpStatus.BAD_GATEWAY),
    GHN_CALCULATE_FEE_FAILED(60002, "Tinh phi GHN that bai" , HttpStatus.BAD_GATEWAY),
    GHN_GET_ORDER_FAILED(60003, "Lay thong tin don GHN that bai" , HttpStatus.BAD_GATEWAY),
    SHIPMENT_NOT_EXIST(60004, "Shipment khong ton tai" , HttpStatus.NOT_FOUND),
    GHN_ORDER_CODE_NOT_EXIST(60005, "Don hang chua co ma GHN" , HttpStatus.BAD_REQUEST),
    COD_NOT_ALLOWED_HIGH_VALUE(60006, "Don hang gia cao nen vui long thanh toan truoc" , HttpStatus.BAD_REQUEST),
    ENDPOINT_NOT_FOUND(90000, "Endpoint khong ton tai" , HttpStatus.NOT_FOUND),
    METHOD_NOT_SUPPORTED(90001, "HTTP method khong duoc ho tro" , HttpStatus.METHOD_NOT_ALLOWED),
    REQUEST_PART_MISSING(90002, "Thieu file hoac request part bat buoc" , HttpStatus.BAD_REQUEST),
    ACCESS_DENIED(90003, "Khong co quyen truy cap" , HttpStatus.FORBIDDEN),
    INTERNAL_SERVER_ERROR(90004, "Loi he thong" , HttpStatus.INTERNAL_SERVER_ERROR),
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
