package com.lichenglin.gulimall.user.exception;

public class PhoneExistsException extends RuntimeException {

    public PhoneExistsException() {
        super("手机号存在");
    }
}
