package com.lichenglin.gulimall.order.vo;

import com.lichenglin.gulimall.order.entity.OrderEntity;
import lombok.Data;

@Data
public class OrderSubmitResponseVo {
    public OrderEntity orderEntity;
    public Integer code;//错误状态码 ： 0 - success ;
}
