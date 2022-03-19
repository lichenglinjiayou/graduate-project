package com.lichenglin.gulimall.product.service.impl;

import com.lichenglin.gulimall.product.entity.AttrAttrgroupRelationEntity;
import com.lichenglin.gulimall.product.entity.AttrEntity;
import com.lichenglin.gulimall.product.entity.ProductAttrValueEntity;
import com.lichenglin.gulimall.product.service.AttrAttrgroupRelationService;
import com.lichenglin.gulimall.product.service.AttrService;
import com.lichenglin.gulimall.product.service.ProductAttrValueService;
import com.lichenglin.gulimall.product.vo.AttrGroupWithAttrsVo;
import com.lichenglin.gulimall.product.vo.SkuItemVo;
import com.lichenglin.gulimall.product.vo.spu.Attr;
import org.apache.commons.lang.StringUtils;
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

import com.lichenglin.gulimall.product.dao.AttrGroupDao;
import com.lichenglin.gulimall.product.entity.AttrGroupEntity;
import com.lichenglin.gulimall.product.service.AttrGroupService;


@Service("attrGroupService")
public class AttrGroupServiceImpl extends ServiceImpl<AttrGroupDao, AttrGroupEntity> implements AttrGroupService {

    @Autowired
    AttrAttrgroupRelationService attrAttrgroupRelationService;

    @Autowired
    AttrService attrService;

    @Autowired
    ProductAttrValueService productAttrValueService;

    @Autowired
    AttrGroupService attrGroupService;

    @Override
    public PageUtils queryPage(Map<String, Object> params) {
        IPage<AttrGroupEntity> page = this.page(
                new Query<AttrGroupEntity>().getPage(params),
                new QueryWrapper<AttrGroupEntity>()
        );

        return new PageUtils(page);
    }

    @Override
    public PageUtils queryPage(Map<String, Object> params, long categoryId) {
        // 获取按照关键字检索时输入的关键字；
        String key = (String) params.get("key");
        // 定义查询规则；
        QueryWrapper<AttrGroupEntity> wrapper = new QueryWrapper<AttrGroupEntity>();
        // 调用StringUtils工具类，判断关键字是否为空，如果不为空，则使用关键字分别对Id字段进行精确分配，对name字段进行模糊匹配
        if(!StringUtils.isEmpty(key)){
            wrapper.and((obj)->{
                obj.eq("attr_group_id",key).or().like("attr_group_name",key);
            });
        }
        // 判断查询是否携带categoryId，如果没有携带，则查询所有的属性信息，如果携带则查询规则中增加按照三级分类查询；
        if(categoryId == 0){
            // 包装成IPage类；
            IPage<AttrGroupEntity> page = this.page(new Query<AttrGroupEntity>().getPage(params),
                   wrapper);
            // 返回PageUtils对象；
            return new PageUtils(page);
        }else{
            wrapper.eq("catelog_id",categoryId);
            IPage<AttrGroupEntity> page = this.page(new Query<AttrGroupEntity>().getPage(params), wrapper);
            return new PageUtils(page);
        }
    }

    @Override
    public List<AttrGroupWithAttrsVo> getAttrGroupWithAttrsByCatelogId(Long catelogId) {

        List<AttrGroupEntity> entities = this.baseMapper.selectList(new QueryWrapper<AttrGroupEntity>().eq("catelog_id", catelogId));
        List<AttrGroupWithAttrsVo> vos = new ArrayList<>();
        entities.forEach((item)->{
            AttrGroupWithAttrsVo attrGroupWithAttrsVo = new AttrGroupWithAttrsVo();
            BeanUtils.copyProperties(item,attrGroupWithAttrsVo);
            List<AttrEntity> attrGroupContainAttr = attrService.getAttrGroupContainAttr(item.getAttrGroupId());
            attrGroupWithAttrsVo.setAttrs(attrGroupContainAttr);
            vos.add(attrGroupWithAttrsVo);
        });
        return vos;
    }

    @Override
    public List<SkuItemVo.SpuItemBaseAttrsGroupVo> getAttrGroupWithAttrsBySpuId(Long spuId,Long catalogId) {
        List<AttrGroupEntity> attrGroupEntities = attrGroupService.list(new QueryWrapper<AttrGroupEntity>().eq("catelog_id", catalogId));
        List<SkuItemVo.SpuItemBaseAttrsGroupVo> spuItemBaseAttrsGroupVoList = new ArrayList<>();
        attrGroupEntities.forEach((item)->{
            SkuItemVo.SpuItemBaseAttrsGroupVo entity = new SkuItemVo.SpuItemBaseAttrsGroupVo();
            entity.setGroupName(item.getAttrGroupName());

            List<SkuItemVo.SpuBaseAttrsVo> spuBaseAttrsVoList = new ArrayList<>();
            List<AttrAttrgroupRelationEntity> attrAttrgroupRelationEntities = attrAttrgroupRelationService.list(new QueryWrapper<AttrAttrgroupRelationEntity>().eq("attr_group_id", item.getAttrGroupId()));
            attrAttrgroupRelationEntities.forEach((obj)->{
                SkuItemVo.SpuBaseAttrsVo spuBaseAttrsVo = new SkuItemVo.SpuBaseAttrsVo();
                AttrEntity attrEntity = attrService.getById(obj.getAttrId());
                spuBaseAttrsVo.setAttrName(attrEntity.getAttrName());
                List<ProductAttrValueEntity> productAttrValueEntities = productAttrValueService.list(new QueryWrapper<ProductAttrValueEntity>().eq("spu_id", spuId).eq("attr_id", obj.getAttrId()));
                List<String> attrValues = new ArrayList<>();
                productAttrValueEntities.forEach((product)->{
                    attrValues.add(product.getAttrValue());
                });
                spuBaseAttrsVo.setAttrValue(attrValues);
                spuBaseAttrsVoList.add(spuBaseAttrsVo);
            });
            entity.setAttrs(spuBaseAttrsVoList);
            spuItemBaseAttrsGroupVoList.add(entity);
        });
        return  spuItemBaseAttrsGroupVoList;
    }
}