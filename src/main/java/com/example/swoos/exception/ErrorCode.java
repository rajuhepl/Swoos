package com.example.swoos.exception;


public enum ErrorCode implements ErrorHandle {

    CAP_1001("1001", "User not found exception"),
    CAP_1002("1002", "Reason Id not found"),
    CAP_1003("1003", "Reason Already Exists"),
    CAP_1017("1017", "Login Failed !!"),
    CAP_1018("1018","Mail Already exists"),
    CAP_1016("1016", "INVALID_CREDENTIALS"),
    CAP_10019("1019","No data found in the specified date range" ),
    CAP_1020("1020", "Failed to write Excel file.");




    private final String errorCode;
    private final String message;

    ErrorCode(String errorCode, String message) {
        this.errorCode = errorCode;
        this.message = message;
    }

    @Override
    public String getErrorCode() {
        return this.errorCode;
    }

    @Override
    public String getMessage() {
        return this.message;
    }

}