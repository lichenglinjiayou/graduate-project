package com.lichenglin.gulimall.product.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.lichenglin.common.utils.PageUtils;
import com.lichenglin.gulimall.product.entity.AttrEntity;
import com.lichenglin.gulimall.product.entity.ProductAttrValueEntity;
import com.lichenglin.gulimall.product.vo.AttrResponseWithPath;
import com.lichenglin.gulimall.product.vo.AttrVo;

import java.util.List;
import java.util.Map;

/**
 * 商品属性
 *
 * @author lichenglin
 * @email 13384966629@163.com
 * @date 2022-02-09 19:42:58
 */
public interface AttrService extends IService<AttrEntity> {

    PageUtils queryPage(Map<String, Object> params);

    void saveAttr(AttrVo attr);

    PageUtils queryWithKey(Map<String, Object> params, Long catelogId, String type);

    AttrResponseWithPath getAllPathById(Long attrId);

    void updateAttrVo(AttrVo attr);

    List<AttrEntity> getAttrGroupContainAttr(Long attrgroupId);

    PageUtils getNoRelationAttr(Map<String, Object> params, Long attrgroupId);

    void updateAttr(Long spuId, List<ProductAttrValueEntity> entities);

    List<Long> selectSearch(List<Long> attrId);
}

