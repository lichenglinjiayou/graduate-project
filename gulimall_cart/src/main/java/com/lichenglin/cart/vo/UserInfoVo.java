package com.lichenglin.cart.vo;

import lombok.Data;
import lombok.ToString;

@ToString
@Data
public class UserInfoVo {


    private String userId;
    private String userKey;
    private Boolean flag = false;
}
