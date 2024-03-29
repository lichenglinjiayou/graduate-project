package com.lichenglin.gulimall.order.dao;

import com.lichenglin.gulimall.order.entity.OrderEntity;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

/**
 * 订单
 * 
 * @author lichenglin
 * @email 13384966629@163.com
 * @date 2022-02-09 21:31:04
 */
@Mapper
public interface OrderDao extends BaseMapper<OrderEntity> {

    void updateOrder(@Param("orderSn") String out_trade_no, @Param("code") Integer code);
}
