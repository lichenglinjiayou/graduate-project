package com.lichenglin.gulimall.product.fallback;

import com.lichenglin.common.exception.BizCodeEnum;
import com.lichenglin.common.utils.R;
import com.lichenglin.gulimall.product.feign.CouponFeign;
import org.springframework.stereotype.Component;

@Component
public class CouponFeignFallBack implements CouponFeign {
    @Override
    public R getSkuSecInfo(Long skuId) {
        return R.error(BizCodeEnum.TOO_MANY_REQUEST.getCode(),BizCodeEnum.TOO_MANY_REQUEST.getMessage());
    }
}
