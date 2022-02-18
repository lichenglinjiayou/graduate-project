package com.lichenglin.gulimall.repository.controller;

import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.Map;

import com.lichenglin.gulimall.repository.vo.MergeVo;
import com.lichenglin.gulimall.repository.vo.PurchaseFinishVo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import com.lichenglin.gulimall.repository.entity.WmsPurchaseEntity;
import com.lichenglin.gulimall.repository.service.WmsPurchaseService;
import com.lichenglin.common.utils.PageUtils;
import com.lichenglin.common.utils.R;



/**
 * 采购信息
 *
 * @author lichenglin
 * @email 13384966629@163.com
 * @date 2022-02-09 21:37:34
 */
@RestController
@RequestMapping("ware/purchase")
public class WmsPurchaseController {
    @Autowired
    private WmsPurchaseService wmsPurchaseService;

    /**
     * 列表
     */
    @RequestMapping("/list")
    public R list(@RequestParam Map<String, Object> params){
        PageUtils page = wmsPurchaseService.queryPage(params);

        return R.ok().put("page", page);
    }

    @GetMapping("/unreceive/list")
    public R unreceiveList(@RequestParam Map<String, Object> params){
        PageUtils page = wmsPurchaseService.queryPurchaseByStatus(params);
        return R.ok().put("page",page);
    }

    /**
     * 信息
     */
    @RequestMapping("/info/{id}")
    public R info(@PathVariable("id") Long id){
		WmsPurchaseEntity wmsPurchase = wmsPurchaseService.getById(id);

        return R.ok().put("wmsPurchase", wmsPurchase);
    }

    /**
     * 保存
     */
    @RequestMapping("/save")
    public R save(@RequestBody WmsPurchaseEntity wmsPurchase){
        wmsPurchase.setUpdateTime(new Date());
        wmsPurchase.setCreateTime(new Date());
		wmsPurchaseService.save(wmsPurchase);

        return R.ok();
    }

    /**
     * 修改
     */
    @RequestMapping("/update")
    public R update(@RequestBody WmsPurchaseEntity wmsPurchase){
		wmsPurchaseService.updateById(wmsPurchase);

        return R.ok();
    }

    /**
     * 删除
     */
    @RequestMapping("/delete")
    public R delete(@RequestBody Long[] ids){
		wmsPurchaseService.removeByIds(Arrays.asList(ids));

        return R.ok();
    }

    @PostMapping("/merge")
    public R merger(@RequestBody MergeVo mergeVo){
        wmsPurchaseService.mergePurchase(mergeVo);
        return R.ok();
    }

    @PostMapping("/received")
    public R receivePurcharseDetail(@RequestBody List<Long> ids){
        wmsPurchaseService.received(ids);
        return R.ok();
    }


    @PostMapping("/done")
    public R finishPurcharseDetail(@RequestBody PurchaseFinishVo purchaseFinishVo){
        wmsPurchaseService.finshed(purchaseFinishVo);
        return R.ok();
    }
}
