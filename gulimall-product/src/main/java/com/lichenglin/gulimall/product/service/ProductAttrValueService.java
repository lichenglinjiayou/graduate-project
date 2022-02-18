package com.lichenglin.gulimall.product.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.lichenglin.common.utils.PageUtils;
import com.lichenglin.gulimall.product.entity.ProductAttrValueEntity;
import com.lichenglin.gulimall.product.vo.spu.BaseAttrs;

import java.util.List;
import java.util.Map;

/**
 * spu属性值
 *
 * @author lichenglin
 * @email 13384966629@163.com
 * @date 2022-02-09 19:42:58
 */
public interface ProductAttrValueService extends IService<ProductAttrValueEntity> {

    PageUtils queryPage(Map<String, Object> params);

    void saveSpuProcutAttrValue(Long spuId, List<BaseAttrs> baseAttrs);

    List<ProductAttrValueEntity> baseAttrListForSpu(Long spuId);
}

