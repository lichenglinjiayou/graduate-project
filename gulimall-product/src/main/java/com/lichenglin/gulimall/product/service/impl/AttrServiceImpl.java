package com.lichenglin.gulimall.product.service.impl;

import com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper;
import com.lichenglin.common.constant.ProductConstant;
import com.lichenglin.gulimall.product.dao.AttrAttrgroupRelationDao;
import com.lichenglin.gulimall.product.dao.AttrGroupDao;
import com.lichenglin.gulimall.product.dao.CategoryDao;
import com.lichenglin.gulimall.product.entity.*;
import com.lichenglin.gulimall.product.service.ProductAttrValueService;
import com.lichenglin.gulimall.product.vo.AttrResponseVo;
import com.lichenglin.gulimall.product.vo.AttrResponseWithPath;
import com.lichenglin.gulimall.product.vo.AttrVo;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.lichenglin.common.utils.PageUtils;
import com.lichenglin.common.utils.Query;

import com.lichenglin.gulimall.product.dao.AttrDao;
import com.lichenglin.gulimall.product.service.AttrService;
import org.springframework.transaction.annotation.Transactional;


@Service("attrService")
public class AttrServiceImpl extends ServiceImpl<AttrDao, AttrEntity> implements AttrService {

    @Autowired
    private AttrAttrgroupRelationDao attrAttrgroupRelationDao;
    @Autowired
    private AttrGroupDao attrGroupDao;
    @Autowired
    private CategoryDao categoryDao;
    @Autowired
    private AttrDao attrDao;
    @Autowired
    private ProductAttrValueService productAttrValueService;
    @Override
    public PageUtils queryPage(Map<String, Object> params) {
        IPage<AttrEntity> page = this.page(
                new Query<AttrEntity>().getPage(params),
                new QueryWrapper<AttrEntity>()
        );

        return new PageUtils(page);
    }

    @Transactional
    @Override
    public void saveAttr(AttrVo attr) {
//        this.save(new AttrEntity(null,attr.getAttrName(),attr.getSearchType(),attr.getValueType(),
//                attr.getIcon(),attr.getValueSelect(),attr.getAttrType(),attr.getEnable(),attr.getCatelogId(),attr.getShowDesc()));
        AttrEntity attrEntity = new AttrEntity();
        BeanUtils.copyProperties(attr,attrEntity);
        this.save(attrEntity);
//        2 保存关联关系
        //使用枚举类替换直接写0/1，以后进行代码修改，只需要修改枚举类即可
        if(attr.getAttrType() == ProductConstant.AttrEnum.ATTR_TYPE_BASE.getCode() && attr.getAttrGroupId() != null){
            AttrAttrgroupRelationEntity attrAttrgroupRelationEntity = new AttrAttrgroupRelationEntity();
            attrAttrgroupRelationEntity.setAttrGroupId(attr.getAttrGroupId());
            attrAttrgroupRelationEntity.setAttrId(attrEntity.getAttrId());
            attrAttrgroupRelationDao.insert(attrAttrgroupRelationEntity);
        }

    }

    @Override
    public PageUtils queryWithKey(Map<String, Object> params, Long catelogId, String type) {
        QueryWrapper<AttrEntity> wrapper = new QueryWrapper<AttrEntity>().eq("attr_type","base".equalsIgnoreCase(type)?ProductConstant.AttrEnum.ATTR_TYPE_BASE.getCode():ProductConstant.AttrEnum.ATTR_TYPE_SALE.getCode());
        String key = (String) params.get("key");
        if(!StringUtils.isEmpty(key)){
            wrapper.and((obj)->{
                obj.eq("attr_id",key).or().like("attr_name",key).or().like("value_select",key);
            });
        }
        if(catelogId != 0){
            wrapper.eq("catelog_id",catelogId);
        }
        IPage<AttrEntity> page = this.page(new Query<AttrEntity>().getPage(params),wrapper);

        PageUtils pageUtils = new PageUtils(page);
        List<AttrEntity> records = page.getRecords();
        List<AttrResponseVo> returnSet = new ArrayList<>();
        records.forEach((attrEntity) -> {
            AttrResponseVo attrResponseVo = new AttrResponseVo();
            BeanUtils.copyProperties(attrEntity,attrResponseVo);
            if("base".equalsIgnoreCase(type)){
                //注入group name
                AttrAttrgroupRelationEntity relationEntity = attrAttrgroupRelationDao.selectOne(new QueryWrapper<AttrAttrgroupRelationEntity>().eq("attr_id", attrEntity.getAttrId()));
                if(relationEntity != null) {
                    Long groupId = relationEntity.getAttrGroupId();
                    AttrGroupEntity attrGroupEntity = attrGroupDao.selectById(groupId);
                    attrResponseVo.setAttrGroupName(attrGroupEntity.getAttrGroupName());
                }
            }

            //注入category name
            CategoryEntity entity = categoryDao.selectById(attrEntity.getCatelogId());
            if(entity != null){
                attrResponseVo.setAttrCatelogName(entity.getName());
            }
            returnSet.add(attrResponseVo);
        });
        pageUtils.setList(returnSet);
        return pageUtils;
    }

    @Override
    public AttrResponseWithPath getAllPathById(Long attrId) {
        AttrEntity attrEntity = this.getById(attrId);
        AttrResponseWithPath attrResponseWithPath = new AttrResponseWithPath();
        BeanUtils.copyProperties(attrEntity,attrResponseWithPath);
        //分类完整路径注入
        Long catelogId = attrEntity.getCatelogId();
        List<Long> list = new ArrayList<>();
        getAllPath(catelogId,list);
        Collections.reverse(list);
        attrResponseWithPath.setCatelogPath(list);

        // 2 判断属性类型，只有基本属性才需要设置attrGroupId/attrGroupName
        if(attrResponseWithPath.getAttrType() == ProductConstant.AttrEnum.ATTR_TYPE_BASE.getCode()) {
            AttrAttrgroupRelationEntity relationEntity = attrAttrgroupRelationDao.selectOne(new QueryWrapper<AttrAttrgroupRelationEntity>().eq("attr_id", attrId));
            Long groupId = relationEntity.getAttrGroupId();
            attrResponseWithPath.setAttrGroupId(groupId);

            AttrGroupEntity attrGroupEntity = attrGroupDao.selectById(groupId);
            String name = attrGroupEntity.getAttrGroupName();
            attrResponseWithPath.setAttrGroupName(name);
        }
        return attrResponseWithPath;
    }

    @Override
    public void updateAttrVo(AttrVo attr) {
        AttrEntity attrEntity = new AttrEntity();
        BeanUtils.copyProperties(attr,attrEntity);
        this.updateById(attrEntity);


        //2 判断属性类型
        if(attrEntity.getAttrType() == ProductConstant.AttrEnum.ATTR_TYPE_BASE.getCode()){
            Integer count = attrAttrgroupRelationDao.selectCount(new QueryWrapper<AttrAttrgroupRelationEntity>().eq("attr_id", attrEntity.getAttrId()));
            AttrAttrgroupRelationEntity relationEntity = new AttrAttrgroupRelationEntity();
            relationEntity.setAttrGroupId(attr.getAttrGroupId());
            relationEntity.setAttrId(attr.getAttrId());
            if(count>0){
                attrAttrgroupRelationDao.update(relationEntity,new UpdateWrapper<AttrAttrgroupRelationEntity>().eq("attr_id",attrEntity.getAttrId()));
            }else{
                attrAttrgroupRelationDao.insert(relationEntity);
            }
        }
    }

    //2 根据分组Id,查找关联的所有属性
    @Override
    public List<AttrEntity> getAttrGroupContainAttr(Long attrgroupId) {
        List<AttrEntity> list = new ArrayList<>();
        List<AttrAttrgroupRelationEntity> entityList = attrAttrgroupRelationDao.selectList(new QueryWrapper<AttrAttrgroupRelationEntity>().eq("attr_group_id", attrgroupId));
        entityList.forEach((obj)->{
            Long attrId = obj.getAttrId();
            AttrEntity attrEntity = attrDao.selectById(attrId);
            list.add(attrEntity);
        });
        if(list.size() == 0){
            return null;
        }
        return list;
    }

    /*
     3. 获取当前分组没有关联的属性
     */
    @Override
    public PageUtils getNoRelationAttr(Map<String, Object> params, Long attrgroupId) {
        // 3 当前分组只能关联自己所属分类里面的属性
        AttrGroupEntity groupEntity = attrGroupDao.selectById(attrgroupId);
        Long catelogId = groupEntity.getCatelogId();
        // 3 查看别的分组没有引用的属性
        //     获取所有分类下的groupId;
        List<AttrGroupEntity> groupEntities = attrGroupDao.selectList(new QueryWrapper<AttrGroupEntity>().eq("catelog_id", catelogId));
        List<Long> attrGroupIdlist = new ArrayList<>();
        groupEntities.forEach((obj)->{
            attrGroupIdlist.add(obj.getAttrGroupId());
        });
        //     获取所有groupId下的attrId；
        List<AttrAttrgroupRelationEntity> entityList = attrAttrgroupRelationDao.selectList(new QueryWrapper<AttrAttrgroupRelationEntity>().in("attr_group_id", attrGroupIdlist));
        List<Long> attrIdList = new ArrayList<>();
        entityList.forEach((obj)->{
            attrIdList.add(obj.getAttrId());
        });
        QueryWrapper<AttrEntity> wrapper = new QueryWrapper<AttrEntity>().eq("catelog_id", catelogId).eq("attr_type",ProductConstant.AttrEnum.ATTR_TYPE_BASE.getCode());
        if(attrIdList.size() > 0){
            wrapper.notIn("attr_id", attrIdList);
        }
        // 3 判断是否有模糊查询条件
        String key = (String) params.get("key");
        if(!StringUtils.isEmpty(key)){
            wrapper.eq("attr_id",key).or().like("attr_name",key).or().like("value_select",key);
        }
        IPage<AttrEntity> page = this.page(new Query<AttrEntity>().getPage(params),wrapper);

        return new PageUtils(page);
    }

    @Override
    public void updateAttr(Long spuId, List<ProductAttrValueEntity> entities) {
        productAttrValueService.remove(new QueryWrapper<ProductAttrValueEntity>().eq("spu_id",spuId));
        List<ProductAttrValueEntity> list = new ArrayList<>();
        entities.forEach((item)->{
            ProductAttrValueEntity productAttrValueEntity = new ProductAttrValueEntity();
            BeanUtils.copyProperties(item,productAttrValueEntity);
            productAttrValueEntity.setSpuId(spuId);
            list.add(productAttrValueEntity);
        });
        productAttrValueService.saveBatch(list);
    }

    public void getAllPath(Long catelogId,List<Long> list){
        list.add(catelogId);
        CategoryEntity entity = categoryDao.selectById(catelogId);
        if(entity.getParentCid() != 0){
            getAllPath(entity.getParentCid(),list);
        }
    }

}