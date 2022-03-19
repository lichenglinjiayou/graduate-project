package com.lichenglin.gulimall.order.vo;

import lombok.Data;

import java.math.BigDecimal;

/**
 * 封装订单提交的数据
 */
@Data
public class OrderSubmitVo {

    private Long addrId;
    private Integer payMode;
    //无需提交购买的商品信息，因为购物车还需要再获取一遍，得到最新的信息；
    //防重令牌
    private String orderToken;
    //验价
    private BigDecimal payPrice;
    //用户相关信息在session中，可以直接从session中获取；
    private String note;//订单备注

}
