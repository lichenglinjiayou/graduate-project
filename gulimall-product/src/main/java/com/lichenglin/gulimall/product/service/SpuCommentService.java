package com.lichenglin.gulimall.product.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.lichenglin.common.utils.PageUtils;
import com.lichenglin.gulimall.product.entity.SpuCommentEntity;

import java.util.Map;

/**
 * 商品评价
 *
 * @author lichenglin
 * @email 13384966629@163.com
 * @date 2022-02-09 19:42:58
 */
public interface SpuCommentService extends IService<SpuCommentEntity> {

    PageUtils queryPage(Map<String, Object> params);
}

