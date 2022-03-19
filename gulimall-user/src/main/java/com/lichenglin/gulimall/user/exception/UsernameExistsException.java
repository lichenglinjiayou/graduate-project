package com.lichenglin.gulimall.user.exception;

public class UsernameExistsException extends RuntimeException {

    public UsernameExistsException() {
        super("用户名存在");
    }
}
