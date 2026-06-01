package org.example.stationery_shop.exception;

public class AppException extends RuntimeException {
    private ErrorCode errorCode;
    private String customMessage;

    public AppException(ErrorCode errorCode) {
        this.errorCode = errorCode;
    }

    public AppException(ErrorCode errorCode, String customMessage) {
        super(customMessage);
        this.errorCode = errorCode;
        this.customMessage = customMessage;
    }

    public ErrorCode getErrorCode() {
        return errorCode;
    }
    public void setErrorCode(ErrorCode errorCode) {
        this.errorCode = errorCode;
    }

    public String getCustomMessage() {
        return customMessage;
    }
}
