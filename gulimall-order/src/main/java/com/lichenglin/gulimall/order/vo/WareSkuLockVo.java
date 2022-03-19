package com.lichenglin.gulimall.order.vo;

import lombok.Data;

import java.util.List;

@Data
public class WareSkuLockVo {

    private String orderSn;
    private List<OrderItemVo> locks;//需要锁的货物数量信息
}
