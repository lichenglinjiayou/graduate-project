package com.lichenglin.gulimall.product.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.lichenglin.common.utils.PageUtils;
import com.lichenglin.gulimall.product.entity.AttrGroupEntity;
import com.lichenglin.gulimall.product.vo.AttrGroupWithAttrsVo;

import java.util.List;
import java.util.Map;

/**
 * 属性分组
 *
 * @author lichenglin
 * @email 13384966629@163.com
 * @date 2022-02-09 19:42:58
 */
public interface AttrGroupService extends IService<AttrGroupEntity> {

    PageUtils queryPage(Map<String, Object> params);

    PageUtils queryPage(Map<String, Object> params, long categoryId);

    List<AttrGroupWithAttrsVo> getAttrGroupWithAttrsByCatelogId(Long catelogId);
}

