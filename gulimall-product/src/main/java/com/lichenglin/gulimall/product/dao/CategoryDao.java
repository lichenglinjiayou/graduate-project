package com.lichenglin.gulimall.product.dao;

import com.lichenglin.gulimall.product.entity.CategoryEntity;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import org.springframework.stereotype.Component;

/**
 * 商品三级分类
 * 
 * @author lichenglin
 * @email 13384966629@163.com
 * @date 2022-02-09 19:42:58
 */
@Mapper
@Component
public interface CategoryDao extends BaseMapper<CategoryEntity> {
	
}
