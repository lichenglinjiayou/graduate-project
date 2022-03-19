package com.lichenglin.common.exception;

public enum BizCodeEnum {
    UNKNOW_EXCEPTION(10000,"Unknown System exception"),
    VALIDATE_EXCEPTION(10001,"Parameter format verification failed"),
    VALIDATE_SMS_CODE_EXCEPTION(10002, "SMS verification code frequency is too high"),
    PRODUCT_UP_EXCEPTION(11000,"customer has exception"),
    USER_EXIST_EXCEPTION(15001,"user has existed"),
    TELEPHONE_EXIST_EXCEPTION(15002,"telephone has existed"),
    LOGIN_FAILED_EXCEPTION(15003,"username or password error"),
    NO_STOCK_EXCEPTION(21000,"commodity is not enough"),
    TOO_MANY_REQUEST(10003,"too many request");


    private int code;
    private String message;

    BizCodeEnum(int code, String message) {
        this.code = code;
        this.message = message;
    }

    public int getCode() {
        return code;
    }

    public String getMessage() {
        return message;
    }
}
