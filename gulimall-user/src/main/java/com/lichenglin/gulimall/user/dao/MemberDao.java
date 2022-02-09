package com.lichenglin.gulimall.user.dao;

import com.lichenglin.gulimall.user.entity.MemberEntity;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;

/**
 * 会员
 * 
 * @author lichenglin
 * @email 13384966629@163.com
 * @date 2022-02-09 20:33:32
 */
@Mapper
public interface MemberDao extends BaseMapper<MemberEntity> {
	
}
