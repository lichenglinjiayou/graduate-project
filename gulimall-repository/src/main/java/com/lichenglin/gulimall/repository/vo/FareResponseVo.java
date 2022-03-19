package com.lichenglin.gulimall.repository.vo;

import lombok.Data;

import java.math.BigDecimal;

@Data
public class FareResponseVo {

    private MemberAddressVo memberAddressVo;
    private BigDecimal fare;
}
