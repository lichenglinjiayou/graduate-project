package com.lichenglin.gulimall.repository.controller;

import java.util.Arrays;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.lichenglin.gulimall.repository.entity.WmsPurchaseDetailEntity;
import com.lichenglin.gulimall.repository.service.WmsPurchaseDetailService;
import com.lichenglin.common.utils.PageUtils;
import com.lichenglin.common.utils.R;



/**
 * 
 *
 * @author lichenglin
 * @email 13384966629@163.com
 * @date 2022-02-09 21:37:34
 */
@RestController
@RequestMapping("ware/purchasedetail")
public class WmsPurchaseDetailController {
    @Autowired
    private WmsPurchaseDetailService wmsPurchaseDetailService;

    /**
     * 列表
     */
    @RequestMapping("/list")
    public R list(@RequestParam Map<String, Object> params){
        PageUtils page = wmsPurchaseDetailService.queryPageByCondition(params);

        return R.ok().put("page", page);
    }


    /**
     * 信息
     */
    @RequestMapping("/info/{id}")
    public R info(@PathVariable("id") Long id){
		WmsPurchaseDetailEntity wmsPurchaseDetail = wmsPurchaseDetailService.getById(id);

        return R.ok().put("wmsPurchaseDetail", wmsPurchaseDetail);
    }

    /**
     * 保存
     */
    @RequestMapping("/save")
    public R save(@RequestBody WmsPurchaseDetailEntity wmsPurchaseDetail){
		wmsPurchaseDetailService.save(wmsPurchaseDetail);

        return R.ok();
    }

    /**
     * 修改
     */
    @RequestMapping("/update")
    public R update(@RequestBody WmsPurchaseDetailEntity wmsPurchaseDetail){
		wmsPurchaseDetailService.updateById(wmsPurchaseDetail);

        return R.ok();
    }

    /**
     * 删除
     */
    @RequestMapping("/delete")
    public R delete(@RequestBody Long[] ids){
		wmsPurchaseDetailService.removeByIds(Arrays.asList(ids));

        return R.ok();
    }

}
