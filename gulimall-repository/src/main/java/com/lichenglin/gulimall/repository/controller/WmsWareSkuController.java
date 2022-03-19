package com.lichenglin.gulimall.repository.controller;

import java.util.Arrays;
import java.util.List;
import java.util.Map;

import com.lichenglin.common.exception.BizCodeEnum;
import com.lichenglin.gulimall.repository.vo.LockStockResultVo;
import com.lichenglin.gulimall.repository.vo.SkuHasStockVo;
import com.lichenglin.gulimall.repository.vo.WareSkuLockVo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import com.lichenglin.gulimall.repository.entity.WmsWareSkuEntity;
import com.lichenglin.gulimall.repository.service.WmsWareSkuService;
import com.lichenglin.common.utils.PageUtils;
import com.lichenglin.common.utils.R;



/**
 * 商品库存
 *
 * @author lichenglin
 * @email 13384966629@163.com
 * @date 2022-02-09 21:37:34
 */
@RestController
@RequestMapping("ware/waresku")
public class WmsWareSkuController {
    @Autowired
    private WmsWareSkuService wmsWareSkuService;

    /**
     * 查询sku是否有库存
     */
    @PostMapping("/hasStock")
    public R getSkusHasStock(@RequestBody List<Long> skuIds){
        List<SkuHasStockVo> list = wmsWareSkuService.hasStock(skuIds);
        R ok = R.ok();

        return ok.setData(list);
    }
    /**
     * 列表
     */
    @RequestMapping("/list")
    public R list(@RequestParam Map<String, Object> params){
        PageUtils page = wmsWareSkuService.queryPageByCondition(params);

        return R.ok().put("page", page);
    }


    /**
     * 信息
     */
    @RequestMapping("/info/{id}")
    public R info(@PathVariable("id") Long id){
		WmsWareSkuEntity wmsWareSku = wmsWareSkuService.getById(id);

        return R.ok().put("wmsWareSku", wmsWareSku);
    }

    /**
     * 保存
     */
    @RequestMapping("/save")
    public R save(@RequestBody WmsWareSkuEntity wmsWareSku){
		wmsWareSkuService.save(wmsWareSku);

        return R.ok();
    }

    /**
     * 修改
     */
    @RequestMapping("/update")
    public R update(@RequestBody WmsWareSkuEntity wmsWareSku){
		wmsWareSkuService.updateById(wmsWareSku);

        return R.ok();
    }

    /**
     * 删除
     */
    @RequestMapping("/delete")
    public R delete(@RequestBody Long[] ids){
		wmsWareSkuService.removeByIds(Arrays.asList(ids));

        return R.ok();
    }

    @PostMapping("/lock")
    public R orderLockStock(@RequestBody WareSkuLockVo wareSkuLockVo){
        Boolean result = null;
        try {
            result = wmsWareSkuService.lockOrder(wareSkuLockVo);
            return R.ok().setData(result);
        } catch (Exception e) {
//            e.printStackTrace();
            return R.error(BizCodeEnum.NO_STOCK_EXCEPTION.getCode(),BizCodeEnum.NO_STOCK_EXCEPTION.getMessage());
        }
    }

}
