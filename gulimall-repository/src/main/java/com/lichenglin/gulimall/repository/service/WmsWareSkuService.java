package com.lichenglin.gulimall.repository.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.lichenglin.common.utils.PageUtils;
import com.lichenglin.gulimall.repository.entity.WmsWareSkuEntity;

import java.util.Map;

/**
 * 商品库存
 *
 * @author lichenglin
 * @email 13384966629@163.com
 * @date 2022-02-09 21:37:34
 */
public interface WmsWareSkuService extends IService<WmsWareSkuEntity> {

    PageUtils queryPage(Map<String, Object> params);

    PageUtils queryPageByCondition(Map<String, Object> params);

    void saveSkuInfo(WmsWareSkuEntity wmsWareSkuEntity);
}

