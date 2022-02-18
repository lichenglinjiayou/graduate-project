package com.lichenglin.gulimall.product.service.impl;

import com.lichenglin.gulimall.product.service.AttrService;
import com.lichenglin.gulimall.product.vo.spu.BaseAttrs;
import org.springframework.beans.BeanUtils;
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

import com.lichenglin.gulimall.product.dao.ProductAttrValueDao;
import com.lichenglin.gulimall.product.entity.ProductAttrValueEntity;
import com.lichenglin.gulimall.product.service.ProductAttrValueService;


@Service("productAttrValueService")
public class ProductAttrValueServiceImpl extends ServiceImpl<ProductAttrValueDao, ProductAttrValueEntity> implements ProductAttrValueService {

    @Autowired
    AttrService attrService;
    @Override
    public PageUtils queryPage(Map<String, Object> params) {
        IPage<ProductAttrValueEntity> page = this.page(
                new Query<ProductAttrValueEntity>().getPage(params),
                new QueryWrapper<ProductAttrValueEntity>()
        );

        return new PageUtils(page);
    }

    @Override
    public void saveSpuProcutAttrValue(Long spuId, List<BaseAttrs> baseAttrs) {
        List<ProductAttrValueEntity> productAttrValueEntities = new ArrayList<>();
        baseAttrs.forEach((item)->{
            ProductAttrValueEntity productAttrValueEntity = new ProductAttrValueEntity();
            productAttrValueEntity.setSpuId(spuId);
            productAttrValueEntity.setAttrId(item.getAttrId());
            productAttrValueEntity.setAttrValue(item.getAttrValues());
            productAttrValueEntity.setAttrName(attrService.getById(item.getAttrId()).getAttrName());
            productAttrValueEntity.setQuickShow(item.getShowDesc());
            productAttrValueEntities.add(productAttrValueEntity);
        });
        this.saveBatch(productAttrValueEntities);
    }

    @Override
    public List<ProductAttrValueEntity> baseAttrListForSpu(Long spuId) {

        List<ProductAttrValueEntity> productAttrValueEntities = this.baseMapper.selectList(new QueryWrapper<ProductAttrValueEntity>().eq("spu_id", spuId));

        return productAttrValueEntities;

    }

}