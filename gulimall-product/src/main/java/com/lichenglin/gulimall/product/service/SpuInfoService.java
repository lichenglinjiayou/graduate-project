package com.lichenglin.gulimall.product.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.lichenglin.common.utils.PageUtils;
import com.lichenglin.gulimall.product.entity.SpuInfoDescEntity;
import com.lichenglin.gulimall.product.entity.SpuInfoEntity;
import com.lichenglin.gulimall.product.vo.spu.SpuSaveVo;

import java.util.Map;

/**
 * spu信息
 *
 * @author lichenglin
 * @email 13384966629@163.com
 * @date 2022-02-09 19:42:58
 */
public interface SpuInfoService extends IService<SpuInfoEntity> {

    PageUtils queryPage(Map<String, Object> params);

    void saveSpuInfo(SpuSaveVo spuSaveVo);
    void saveBaseSpuInfo(SpuInfoEntity spuInfoEntity);

    void saveSpuDescImageInfo(SpuInfoDescEntity spuInfoDescEntity);

    PageUtils queryPageByCondition(Map<String, Object> params);
}

