package com.lichenglin.gulimall.product.controller;

import java.util.Arrays;
import java.util.List;
import java.util.Map;

import com.lichenglin.gulimall.product.dao.AttrAttrgroupRelationDao;
import com.lichenglin.gulimall.product.entity.AttrAttrgroupRelationEntity;
import com.lichenglin.gulimall.product.entity.AttrEntity;
import com.lichenglin.gulimall.product.service.AttrAttrgroupRelationService;
import com.lichenglin.gulimall.product.service.AttrService;
import com.lichenglin.gulimall.product.service.CategoryService;
import com.lichenglin.gulimall.product.vo.AttrAttrGroupRelationVo;
import com.lichenglin.gulimall.product.vo.AttrGroupWithAttrsVo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import com.lichenglin.gulimall.product.entity.AttrGroupEntity;
import com.lichenglin.gulimall.product.service.AttrGroupService;
import com.lichenglin.common.utils.PageUtils;
import com.lichenglin.common.utils.R;



/**
 * 属性分组
 *
 * @author lichenglin
 * @email 13384966629@163.com
 * @date 2022-02-09 19:42:58
 */
@RestController
@RequestMapping("product/attrgroup")
public class AttrGroupController {
    @Autowired
    private AttrGroupService attrGroupService;

    @Autowired
    private CategoryService categoryService;

    @Autowired
    private AttrService attrService;

    @Autowired
    private AttrAttrgroupRelationService attrAttrgroupRelationService;

    /**
     * 列表
     */
    @RequestMapping("/list/{categoryId}")
    public R list(@RequestParam Map<String, Object> params,@PathVariable("categoryId")long categoryId){
        //自定义查询方法，增加查询categoryId;
        PageUtils page = attrGroupService.queryPage(params,categoryId);
        return R.ok().put("page", page);
    }


    /**
     * 信息
     */
    @RequestMapping("/info/{attrGroupId}")
    public R info(@PathVariable("attrGroupId") Long attrGroupId){
		AttrGroupEntity attrGroup = attrGroupService.getById(attrGroupId);
        Long catelogId = attrGroup.getCatelogId();
        // 自定义方法，根据attrGroupId获取到完整的路径，并增加到attrGroup中，返回；
        // 在AttrGroupEntity中增加categoryIds[]属性字段；
        Long[] catelogIds = categoryService.findCatelogIds(catelogId);
        attrGroup.setCatelogIds(catelogIds);
        return R.ok().put("attrGroup", attrGroup);
    }

    @GetMapping("/{attrgroupId}/attr/relation")
    public R attrRelation(@PathVariable("attrgroupId") Long attrgroupId){
        List<AttrEntity> attrEntities = attrService.getAttrGroupContainAttr(attrgroupId);
        return R.ok().put("data",attrEntities);
    }
    /*
        3 查询没有关联的全部属性
     */
    @GetMapping("/{attrgroupId}/noattr/relation")
    public R attrNoRelation(@RequestParam Map<String, Object> params,@PathVariable("attrgroupId") Long attrgroupId){
        PageUtils page = attrService.getNoRelationAttr(params,attrgroupId);
        return R.ok().put("page",page);
    }

    @GetMapping("/{catelogId}/withattr")
    public R getAttrGroupWithAttrs(@PathVariable("catelogId") Long catelogId){
        // 3 查出当前分类下的所有属性分组；
        // 3 查看属性分组下的所有属性
        List<AttrGroupWithAttrsVo> list = attrGroupService.getAttrGroupWithAttrsByCatelogId(catelogId);
        return R.ok().put("data",list);
    }

    /**
     * 保存
     */
    @RequestMapping("/save")
    public R save(@RequestBody AttrGroupEntity attrGroup){
		attrGroupService.save(attrGroup);

        return R.ok();
    }

    @PostMapping("/attr/relation")
    public R saveAttrRelation(@RequestBody List<AttrAttrGroupRelationVo> attrAttrGroupRelationVo){
        attrAttrgroupRelationService.saveAttrRelation(attrAttrGroupRelationVo);
        return R.ok();
    }

    /**
     * 修改
     */
    @RequestMapping("/update")
    public R update(@RequestBody AttrGroupEntity attrGroup){
		attrGroupService.updateById(attrGroup);

        return R.ok();
    }

    /**
     * 删除
     */
    @RequestMapping("/delete")
    public R delete(@RequestBody Long[] attrGroupIds){
		attrGroupService.removeByIds(Arrays.asList(attrGroupIds));

        return R.ok();
    }

    @PostMapping("/attr/relation/delete")
    public R delete(@RequestBody AttrAttrGroupRelationVo[] attrAttrGroupRelationVo){
//        attrGroupService.removeByIds(Arrays.asList(attrGroupIds));
        attrAttrgroupRelationService.deleteByAttrIdAndAttrGroupId(attrAttrGroupRelationVo);
        return R.ok();
    }

}
