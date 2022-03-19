package com.lichenglin.seckill.service;

import com.lichenglin.seckill.to.SeckillRedisTo;

import java.util.List;

public interface SeckillService {
    void uploadCommodity();

    List<SeckillRedisTo> getSeckillProducts();

    SeckillRedisTo getSkuKillInfo(Long skuId);

    String handleSecKillRequest(String killId, String randomCode, Integer num);
}
