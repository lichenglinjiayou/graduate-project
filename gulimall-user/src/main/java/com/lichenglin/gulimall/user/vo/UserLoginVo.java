package com.lichenglin.gulimall.user.vo;

import lombok.Data;

@Data
public class UserLoginVo {
    private Long userId;
    private String loginAccount;
    private String password;
}
