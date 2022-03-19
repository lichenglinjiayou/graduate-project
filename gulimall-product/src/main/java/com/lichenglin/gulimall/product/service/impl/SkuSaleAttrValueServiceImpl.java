package com.lichenglin.gulimall.product.service.impl;

import com.lichenglin.gulimall.product.entity.SkuInfoEntity;
import com.lichenglin.gulimall.product.service.SkuInfoService;
import com.lichenglin.gulimall.product.vo.SkuItemVo;
import com.lichenglin.gulimall.product.vo.spu.SkuItemSaleAttrsVo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.lichenglin.common.utils.PageUtils;
import com.lichenglin.common.utils.Query;

import com.lichenglin.gulimall.product.dao.SkuSaleAttrValueDao;
import com.lichenglin.gulimall.product.entity.SkuSaleAttrValueEntity;
import com.lichenglin.gulimall.product.service.SkuSaleAttrValueService;


@Service("skuSaleAttrValueService")
public class SkuSaleAttrValueServiceImpl extends ServiceImpl<SkuSaleAttrValueDao, SkuSaleAttrValueEntity> implements SkuSaleAttrValueService {

    @Autowired
    SkuInfoService skuInfoService;
    @Autowired
    SkuSaleAttrValueService skuSaleAttrValueService;
    @Override
    public PageUtils queryPage(Map<String, Object> params) {
        IPage<SkuSaleAttrValueEntity> page = this.page(
                new Query<SkuSaleAttrValueEntity>().getPage(params),
                new QueryWrapper<SkuSaleAttrValueEntity>()
        );

        return new PageUtils(page);
    }

    @Override
    public List<SkuItemSaleAttrsVo> getSaleAttrsBySpuId(Long spuId) {
        List<SkuItemSaleAttrsVo> saleAttrsVos = this.baseMapper.getSaleAttrsBySpuId(spuId);
        return saleAttrsVos;
    }

    @Override
    public List<String> getAttrAsStringList(Long skuId) {

        return this.baseMapper.getAttrAsStringList(skuId);
    }

}