package com.lichenglin.gulimall.product.controller;

import java.util.Arrays;
import java.util.List;
import java.util.Map;

import com.lichenglin.gulimall.product.entity.ProductAttrValueEntity;
import com.lichenglin.gulimall.product.service.ProductAttrValueService;
import com.lichenglin.gulimall.product.vo.AttrResponseWithPath;
import com.lichenglin.gulimall.product.vo.AttrVo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import com.lichenglin.gulimall.product.entity.AttrEntity;
import com.lichenglin.gulimall.product.service.AttrService;
import com.lichenglin.common.utils.PageUtils;
import com.lichenglin.common.utils.R;



/**
 * 商品属性
 *
 * @author lichenglin
 * @email 13384966629@163.com
 * @date 2022-02-09 19:42:58
 */
@RestController
@RequestMapping("product/attr")
public class AttrController {
    @Autowired
    private AttrService attrService;
    @Autowired
    private ProductAttrValueService productAttrValueService;

    /**
     * 列表
     */
    @RequestMapping("/list")
    public R list(@RequestParam Map<String, Object> params){
        PageUtils page = attrService.queryPage(params);

        return R.ok().put("page", page);
    }

    @GetMapping("/{AttrType}/list/{catelogId}")
    public R baseList(@RequestParam Map<String, Object> params,@PathVariable("catelogId") Long catelogId,
                      @PathVariable("AttrType") String type){
//        PageUtils page = attrService.queryPage(params);
        PageUtils page = attrService.queryWithKey(params,catelogId,type);

        return R.ok().put("page", page);
    }


    /**
     * 信息
     */
    @RequestMapping("/info/{attrId}")
    public R info(@PathVariable("attrId") Long attrId){
//		AttrEntity attr = attrService.getById(attrId);
        AttrResponseWithPath attr = attrService.getAllPathById(attrId);
        return R.ok().put("attr", attr);
    }

    /**
     * 保存
     */
    @RequestMapping("/save")
    public R save(@RequestBody AttrVo attr){
//		attrService.save(attr);
        attrService.saveAttr(attr);
        return R.ok();
    }

    /**
     * 修改
     */
    @RequestMapping("/update")
    public R update(@RequestBody AttrVo attr){
//		attrService.updateById(attr);
        attrService.updateAttrVo(attr);
        return R.ok();
    }

    @PostMapping("/update/{spuId}")
    public R updateBatchAttr(@RequestBody List<ProductAttrValueEntity> entities,@PathVariable("spuId") Long spuId){
        attrService.updateAttr(spuId,entities);
        return R.ok();
    }

    /**
     * 删除
     */
    @RequestMapping("/delete")
    public R delete(@RequestBody Long[] attrIds){
		attrService.removeByIds(Arrays.asList(attrIds));

        return R.ok();
    }

    @GetMapping("/base/listforspu/{spuId}")
    public R baseAttrList(@PathVariable("spuId") Long spuId){

        List<ProductAttrValueEntity> productAttrValueEntity = productAttrValueService.baseAttrListForSpu(spuId);
        return R.ok().put("data",productAttrValueEntity);
    }

}
