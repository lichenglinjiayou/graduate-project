package com.lichenglin.gulimall.repository.dao;

import com.lichenglin.gulimall.repository.entity.WmsWareSkuEntity;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.lichenglin.gulimall.repository.vo.StackInfoVo;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

/**
 * 商品库存
 * 
 * @author lichenglin
 * @email 13384966629@163.com
 * @date 2022-02-09 21:37:34
 */
@Mapper
public interface WmsWareSkuDao extends BaseMapper<WmsWareSkuEntity> {

    void saveSkuInfo(@Param("wmsWareSkuEntity") WmsWareSkuEntity wmsWareSkuEntity);

    StackInfoVo getStock(@Param("skuId") Long item);

    Long lockSkuStock(@Param("skuId") Long skuId, @Param("wareId") Long wareId, @Param("num") Integer num);

    void unlockStock(@Param("skuId") Long skuId, @Param("wareId") Long wareId, @Param("num") Integer num);
}
