package com.lichenglin.gulimall.product.app;

import java.util.Arrays;
import java.util.List;
import java.util.Map;

import com.lichenglin.common.valid.AddGroup;
import com.lichenglin.common.valid.UpdateGroup;
import com.lichenglin.common.valid.UpdateStatusGroup;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import com.lichenglin.gulimall.product.entity.BrandEntity;
import com.lichenglin.gulimall.product.service.BrandService;
import com.lichenglin.common.utils.PageUtils;
import com.lichenglin.common.utils.R;

import javax.validation.Valid;


/**
 * 品牌
 *
 * @author lichenglin
 * @email 13384966629@163.com
 * @date 2022-02-09 19:42:58
 */
@RestController
@RequestMapping("product/brand")
public class BrandController {
    @Autowired
    private BrandService brandService;

    /**
     * 列表
     */
    @RequestMapping("/list")
    public R list(@RequestParam Map<String, Object> params){
        PageUtils page = brandService.queryPage(params);

        return R.ok().put("page", page);
    }


    /**
     * 信息
     */
    @RequestMapping("/info/{brandId}")
    public R info(@PathVariable("brandId") Long brandId){
		BrandEntity brand = brandService.getById(brandId);

        return R.ok().put("brand", brand);
    }

    @GetMapping("/infos")
    public R info(@RequestParam("brandIds")List<Long> brandIds){
       List<BrandEntity> entities =  brandService.getBrands(brandIds);
       return  R.ok().put("data",entities);
    }

    /**
     * 保存
     */
    @RequestMapping("/save")
    public R save(@Validated({AddGroup.class}) @Valid @RequestBody BrandEntity brand/* BindingResult bindingResult */){
        /*
        if(bindingResult.hasErrors()){
            Map<String,String> errorMap = new HashMap<>();
            bindingResult.getFieldErrors().forEach((item) -> {
//                1 获取到错误校验消息，配了，使用自己的；
               String message =  item.getDefaultMessage();
//                1 获取错误属性的名字；
                String field = item.getField();

                errorMap.put(field,message);
            });
            return R.error(400,"提交数据不合法").put("data",errorMap);
        }else{
         */
            brandService.save(brand);

            return R.ok();

    }

    /**
     * 修改
     */
    @RequestMapping("/update")
    public R update(@Validated(value = {UpdateGroup.class}) @RequestBody BrandEntity brand){
//		brandService.updateById(brand);
        brandService.updateDetails(brand);
        return R.ok();
    }

    /**
     * 修改状态
     */
    @RequestMapping("/update/status")
    public R updateStatus(@Validated(value = {UpdateStatusGroup.class}) @RequestBody BrandEntity brand){
        brandService.updateById(brand);

        return R.ok();
    }

    /**
     * 删除
     */
    @RequestMapping("/delete")
    public R delete(@RequestBody Long[] brandIds){
		brandService.removeByIds(Arrays.asList(brandIds));

        return R.ok();
    }

}
