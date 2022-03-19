package com.lichenglin.gulimall.product.feign;

import com.lichenglin.common.utils.R;
import com.lichenglin.gulimall.product.fallback.CouponFeignFallBack;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

@FeignClient(value = "gulimall-seckill",fallback = CouponFeignFallBack.class)
public interface CouponFeign {

    @GetMapping("/getSkuSecInfo/{skuId}")
    public R getSkuSecInfo(@PathVariable("skuId") Long skuId);
}
