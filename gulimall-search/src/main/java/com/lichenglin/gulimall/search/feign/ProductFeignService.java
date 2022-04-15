package com.lichenglin.gulimall.search.feign;

import com.lichenglin.common.utils.R;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@FeignClient("gulimall-product")
public interface ProductFeignService {
    /**
     * Query attribute name by attribute ID;
     * @param attrId
     * @return
     */
    @GetMapping("/product/attr/info/{attrId}")
    public R attrInfo(@PathVariable("attrId") Long attrId);

    @GetMapping("/product/brand/infos")
    public R brandInfo(@RequestParam("brandIds") List<Long> brandIds);
}
