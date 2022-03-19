package com.lichenglin.gulimall.product.feign;

import com.lichenglin.common.to.SkuReductionTo;
import com.lichenglin.common.to.SpuBoundsTo;
import com.lichenglin.common.utils.R;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.ResponseBody;

@FeignClient("gulimall-coupon")
public interface SpuFeign {

    /*
        3  Step1: SpringCloud将传输的对象转为Json;
           Step2：SpringCloud会在注册中心中寻找名为gulimall-coupon的微服务，发送/coupon/smsspubounds/save请求；
                  将上一步转的json对象放在请求体位置；
           Step3: 对方微服务收到请求，通过@ResponseBody，将请求体的json转为SpuBoundsTo类型(可以类型不匹配，只要JSON数据模型兼容
           即可，双方服务无需使用同一个To)
           Summary: 远程微服务调用，只需要关注远程服务调用的路径，及参数的封装格式即可；
    */
    @PostMapping("/coupon/smsspubounds/save")
    R saveSpuBounds(@RequestBody SpuBoundsTo spuBoundsTo);

    @PostMapping("/coupon/smsskufullreduction/saveInfo")
    R saveSkuReduction(@RequestBody SkuReductionTo skuReductionTo);
}
