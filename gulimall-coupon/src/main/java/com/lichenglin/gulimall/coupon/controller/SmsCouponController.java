package com.lichenglin.gulimall.coupon.controller;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.lichenglin.gulimall.coupon.entity.SmsCouponEntity;
import com.lichenglin.gulimall.coupon.service.SmsCouponService;
import com.lichenglin.common.utils.PageUtils;
import com.lichenglin.common.utils.R;



/**
 * 优惠券信息
 *
 * @author lichenglin
 * @email 13384966629@163.com
 * @date 2022-02-09 20:21:47
 */
@RestController
@RequestMapping("coupon/smscoupon")
@RefreshScope
public class SmsCouponController {
    @Autowired
    private SmsCouponService smsCouponService;

    @Value("${person.name}")
    private String name;

    @Value("${person.age}")
    private Integer age;

    @RequestMapping("/info")
    public R getInfo(){
        return R.ok().put("name",name).put("age",age);
    }

    @RequestMapping("/userCoupon")
    public R userCoupon(){
        SmsCouponEntity smsCouponEntity = new SmsCouponEntity();
        smsCouponEntity.setCouponName("满100减20");
        smsCouponEntity.setAmount(new BigDecimal(20));
        smsCouponEntity.setCode("lcl19960822");
        return R.ok().put("coupons",smsCouponEntity);
    }
    /**
     * 列表
     */
    @RequestMapping("/list")
    public R list(@RequestParam Map<String, Object> params){
        PageUtils page = smsCouponService.queryPage(params);

        return R.ok().put("page", page);
    }


    /**
     * 信息
     */
    @RequestMapping("/info/{id}")
    public R info(@PathVariable("id") Long id){
		SmsCouponEntity smsCoupon = smsCouponService.getById(id);

        return R.ok().put("smsCoupon", smsCoupon);
    }

    /**
     * 保存
     */
    @RequestMapping("/save")
    public R save(@RequestBody SmsCouponEntity smsCoupon){
		smsCouponService.save(smsCoupon);

        return R.ok();
    }

    /**
     * 修改
     */
    @RequestMapping("/update")
    public R update(@RequestBody SmsCouponEntity smsCoupon){
		smsCouponService.updateById(smsCoupon);

        return R.ok();
    }

    /**
     * 删除
     */
    @RequestMapping("/delete")
    public R delete(@RequestBody Long[] ids){
		smsCouponService.removeByIds(Arrays.asList(ids));

        return R.ok();
    }

}
