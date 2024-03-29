package com.lichenglin.gulimall.coupon.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.lichenglin.common.to.SkuReductionTo;
import com.lichenglin.common.utils.PageUtils;
import com.lichenglin.gulimall.coupon.entity.SmsSkuFullReductionEntity;

import java.util.Map;

/**
 * 商品满减信息
 *
 * @author lichenglin
 * @email 13384966629@163.com
 * @date 2022-02-09 20:21:47
 */
public interface SmsSkuFullReductionService extends IService<SmsSkuFullReductionEntity> {

    PageUtils queryPage(Map<String, Object> params);

    void saveSkuReduction(SkuReductionTo skuReductionTo);

}

