package com.lichenglin.gulimall.product.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.lichenglin.common.utils.PageUtils;
import com.lichenglin.gulimall.product.entity.SkuInfoEntity;
import com.lichenglin.gulimall.product.entity.SpuInfoEntity;
import com.lichenglin.gulimall.product.vo.SkuItemVo;
import com.lichenglin.gulimall.product.vo.spu.SpuSaveVo;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

/**
 * sku信息
 *
 * @author lichenglin
 * @email 13384966629@163.com
 * @date 2022-02-09 19:42:58
 */
public interface SkuInfoService extends IService<SkuInfoEntity> {

    PageUtils queryPage(Map<String, Object> params);

    void saveSkuInfo(SpuInfoEntity spuInfoEntity, SpuSaveVo spuSaveVo);

    PageUtils queryPageByCondition(Map<String, Object> params);

    List<SkuInfoEntity> getSkusBySpuId(Long spuId);

    SkuItemVo getSkuInfo(Long skuId);

}

