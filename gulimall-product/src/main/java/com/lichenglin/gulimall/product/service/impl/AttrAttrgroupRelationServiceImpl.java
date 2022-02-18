package com.lichenglin.gulimall.product.service.impl;

import com.lichenglin.gulimall.product.vo.AttrAttrGroupRelationVo;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.lichenglin.common.utils.PageUtils;
import com.lichenglin.common.utils.Query;

import com.lichenglin.gulimall.product.dao.AttrAttrgroupRelationDao;
import com.lichenglin.gulimall.product.entity.AttrAttrgroupRelationEntity;
import com.lichenglin.gulimall.product.service.AttrAttrgroupRelationService;


@Service("attrAttrgroupRelationService")
public class AttrAttrgroupRelationServiceImpl extends ServiceImpl<AttrAttrgroupRelationDao, AttrAttrgroupRelationEntity> implements AttrAttrgroupRelationService {

    @Override
    public PageUtils queryPage(Map<String, Object> params) {
        IPage<AttrAttrgroupRelationEntity> page = this.page(
                new Query<AttrAttrgroupRelationEntity>().getPage(params),
                new QueryWrapper<AttrAttrgroupRelationEntity>()
        );

        return new PageUtils(page);
    }

    @Override
    public void deleteByAttrIdAndAttrGroupId(AttrAttrGroupRelationVo[] attrAttrGroupRelationVos) {
//        this.baseMapper.delete(new QueryWrapper<AttrAttrgroupRelationEntity>().and((obj)->{
//            obj.eq("attr_id",attrAttrGroupRelationVos[0]).eq("attr_group_id",attrAttrGroupRelationVos[1]);
//        }));
        List<AttrAttrgroupRelationEntity> list = new ArrayList<>();
        Arrays.asList(attrAttrGroupRelationVos).forEach((item)->{
            AttrAttrgroupRelationEntity entity = new AttrAttrgroupRelationEntity();
            BeanUtils.copyProperties(item,entity);
            list.add(entity);
        });
        this.baseMapper.deleteBatchRelation(list);
    }

    @Override
    public void saveAttrRelation(List<AttrAttrGroupRelationVo> attrAttrGroupRelationVo) {

        attrAttrGroupRelationVo.forEach((item)->{
            AttrAttrgroupRelationEntity attrAttrgroupRelationEntity = new AttrAttrgroupRelationEntity();
            BeanUtils.copyProperties(item,attrAttrgroupRelationEntity);
            this.baseMapper.insert(attrAttrgroupRelationEntity);
        });
    }

}