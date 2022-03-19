package com.lichenglin.gulimall.repository.vo;

import lombok.Data;

@Data
public class LockStockResultVo {
    private Long skuId; //货物ID
    private Integer num; // 货物数量
    private Boolean lock;//锁定结果
}
