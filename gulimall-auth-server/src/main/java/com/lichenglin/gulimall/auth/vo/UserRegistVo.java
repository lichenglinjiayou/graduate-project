package com.lichenglin.gulimall.auth.vo;

import lombok.Data;
import org.hibernate.validator.constraints.Length;

import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.Pattern;

@Data
public class UserRegistVo {
    @NotEmpty(message = "username is not empty")
    @Length(min = 6,max = 18,message = "username must be 6 - 18 characters")
    private String username;
    @NotEmpty(message = "password is not empty")
    @Length(min = 6,max = 18,message = "password must be 6 - 18 characters")
    @Pattern(regexp = "^[a-z0-9A-Z]{6,18}$",message = "password must be 0-9、a-z、A-Z")
    private String password;
    @Pattern(regexp = "^[1]([3-9])[0-9]{9}$",message = "telephone wrong")
    @NotEmpty(message = "telephone is not empty")
    private String telephone;
    @NotEmpty(message = "code is not empty")
    private String code;
}
