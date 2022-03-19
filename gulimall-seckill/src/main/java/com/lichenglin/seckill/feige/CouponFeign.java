package com.lichenglin.seckill.feige;

import com.lichenglin.common.utils.R;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;

@FeignClient("gulimall-coupon")
public interface CouponFeign {

    @GetMapping("/coupon/smsseckillsession/session")
    public R getSessionLatest3Days();

}
