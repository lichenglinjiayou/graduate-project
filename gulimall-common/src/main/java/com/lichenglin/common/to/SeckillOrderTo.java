package com.lichenglin.common.to;

import lombok.Data;

import java.math.BigDecimal;

@Data
public class SeckillOrderTo {
    private String orderSn;//点单号
    private Long promotionSessionId;//场次ID
    private Long skuId;//商品ID
    private BigDecimal seckillPrice;//商品的活动价格
    private BigDecimal seckillCount;//商品的购买数量
    private String userId;

}
