package com.example.swoos.exception;


public class CustomValidationException extends Exception {

    private static final long serialVersionUID = 2816438424415940868L;

    private final ErrorCode errorCode;


    public CustomValidationException(ErrorCode codes) {
        super(getMessage(codes));
        this.errorCode = codes;
    }

    public ErrorCode getErrorCode() {
        return errorCode;
    }

    private static String getMessage(ErrorCode errorCode) {
        if (errorCode.getMessage() != null) {
            return errorCode.getMessage();
        }else {
            return null;
        }
    }
}

