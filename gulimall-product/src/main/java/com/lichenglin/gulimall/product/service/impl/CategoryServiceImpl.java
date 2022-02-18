package com.lichenglin.gulimall.product.service.impl;

import com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.lichenglin.gulimall.product.entity.CategoryBrandRelationEntity;
import com.lichenglin.gulimall.product.service.CategoryBrandRelationService;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.*;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.lichenglin.common.utils.PageUtils;
import com.lichenglin.common.utils.Query;

import com.lichenglin.gulimall.product.dao.CategoryDao;
import com.lichenglin.gulimall.product.entity.CategoryEntity;
import com.lichenglin.gulimall.product.service.CategoryService;
import org.springframework.transaction.annotation.Transactional;


@Service("categoryService")
public class CategoryServiceImpl extends ServiceImpl<CategoryDao, CategoryEntity> implements CategoryService {

    @Autowired
    CategoryDao categoryDao;

    @Autowired
    private CategoryBrandRelationService categoryBrandRelationService;

    @Override
    public PageUtils queryPage(Map<String, Object> params) {
        IPage<CategoryEntity> page = this.page(
                new Query<CategoryEntity>().getPage(params),
                new QueryWrapper<CategoryEntity>()
        );

        return new PageUtils(page);
    }

    @Override
    public List<CategoryEntity> listByTree() {
        List<CategoryEntity> entities = categoryDao.selectList(null);
        List<CategoryEntity> levelOne = new ArrayList<>();
        for (CategoryEntity entity : entities) {
            if(entity.getParentCid() == 0){
                //找出所有一级分类的商品，添加到list集合中；
                levelOne.add(entity);
            }
        }
        for (CategoryEntity entity : levelOne) {
            //遍历一级商品的集合，对每个一级商品都调用getChildrens()方法；
            getChildrens(entity,entities);
        }
        //对一级商品按照自定义的排序规则进行排序；
        Collections.sort(levelOne, new Comparator<CategoryEntity>() {
            @Override
            public int compare(CategoryEntity o1, CategoryEntity o2) {
                return (o1.getSort()==null ? 0:o1.getSort()) -
                        (o2.getSort()==null ? 0:o2.getSort());
            }
        });
        return levelOne;
    }


    public void getChildrens(CategoryEntity root,List<CategoryEntity> all){
        List<CategoryEntity> child = new ArrayList<>();
        for (CategoryEntity entity : all) {
            //递归1次：找到所有1级商品包含的2级商品；
            //递归2次：找到所有2级商品包含的3级商品；
            if(entity.getParentCid() == root.getCatId()){
                //将符合要求的商品添加到list集合中；
                child.add(entity);
            }
        }
        Collections.sort(child, new Comparator<CategoryEntity>() {
            @Override
            public int compare(CategoryEntity o1, CategoryEntity o2) {
                return (o1.getSort()==null ? 0:o1.getSort()) -
                        (o2.getSort()==null ? 0:o2.getSort());
            }
        });
        //递归1次：设置1级商品的children属性；
        //递归2次：设置2级商品的children属性；
        root.setChildren(child);
        //只有当含有下一级的商品时，才进入下一次递归操作；
        if(child.size() != 0){
            for (CategoryEntity entity : child) {
                getChildrens(entity,all);
            }
        }
    }

    @Override
    public void removeMenuByIds(List<Long> asList) {
//        TODO 检查待删除的菜单，是否在别处被引用
        categoryDao.deleteBatchIds(asList);
    }


    @Override
    public Long[] findCatelogIds(Long catelogId) {
        List<Long> catelogIds = new ArrayList<>();
        getAllParentPath(catelogId,catelogIds);
        Collections.reverse(catelogIds);
        return catelogIds.toArray(new Long[catelogIds.size()]);
    }

    @Override
    @Transactional
    public void updateCascade(CategoryEntity category) {
            this.updateById(category);
            if(!StringUtils.isEmpty(category.getName())){
                CategoryBrandRelationEntity categoryBrandRelationEntity = new CategoryBrandRelationEntity();
                categoryBrandRelationEntity.setCatelogId(category.getCatId());
                categoryBrandRelationEntity.setCatelogName(category.getName());
                categoryBrandRelationService.updateCategory(category.getCatId(),category.getName());
            }
    }

    /**
     * 递归三级目录的方法
     */
    private  List<Long>  getAllParentPath(Long catelogId,List<Long> list){
        list.add(catelogId);
        CategoryEntity parent = this.getById(catelogId);
        if(parent.getParentCid() != 0){
            getAllParentPath(parent.getParentCid(),list);
        }
        return list;
    }

}