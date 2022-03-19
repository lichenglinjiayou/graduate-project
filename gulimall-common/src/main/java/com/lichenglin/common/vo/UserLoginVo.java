package com.lichenglin.common.vo;

import lombok.Data;

import java.io.Serializable;

@Data
public class UserLoginVo implements Serializable {

    private static final long serialVersionUID = 1L;

    private String id;
    private String loginAccount;
    private String password;
}
