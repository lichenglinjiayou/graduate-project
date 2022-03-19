package com.lichenglin.gulimall.user.feign;

import com.lichenglin.common.utils.R;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.annotation.RequestMapping;

@FeignClient("gulimall-coupon")
public interface UserCoupon {

    @RequestMapping("/coupon/smscoupon/userCoupon")
    public R userCoupon();
}
