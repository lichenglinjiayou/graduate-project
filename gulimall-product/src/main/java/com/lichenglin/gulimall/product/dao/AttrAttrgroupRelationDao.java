package com.lichenglin.gulimall.product.dao;

import com.lichenglin.gulimall.product.entity.AttrAttrgroupRelationEntity;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * 属性&属性分组关联
 * 
 * @author lichenglin
 * @email 13384966629@163.com
 * @date 2022-02-09 19:42:58
 */
@Mapper
@Component
public interface AttrAttrgroupRelationDao extends BaseMapper<AttrAttrgroupRelationEntity> {

    void deleteBatchRelation(@Param("entities") List<AttrAttrgroupRelationEntity> list);
}
