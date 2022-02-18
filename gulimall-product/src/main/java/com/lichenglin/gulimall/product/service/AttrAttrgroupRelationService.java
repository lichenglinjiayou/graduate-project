package com.lichenglin.gulimall.product.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.lichenglin.common.utils.PageUtils;
import com.lichenglin.gulimall.product.entity.AttrAttrgroupRelationEntity;
import com.lichenglin.gulimall.product.vo.AttrAttrGroupRelationVo;

import java.util.List;
import java.util.Map;

/**
 * 属性&属性分组关联
 *
 * @author lichenglin
 * @email 13384966629@163.com
 * @date 2022-02-09 19:42:58
 */
public interface AttrAttrgroupRelationService extends IService<AttrAttrgroupRelationEntity> {

    PageUtils queryPage(Map<String, Object> params);

    void deleteByAttrIdAndAttrGroupId(AttrAttrGroupRelationVo[] attrAttrGroupRelationVos);

    void saveAttrRelation(List<AttrAttrGroupRelationVo> attrAttrGroupRelationVo);
}

