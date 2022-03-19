package com.lichenglin.cart.feign;

import com.lichenglin.common.utils.R;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.math.BigDecimal;
import java.util.List;

@FeignClient("gulimall-product")
public interface ProductFeign {
    @RequestMapping("/product/skuinfo/info/{skuId}")
    public R info(@PathVariable("skuId") Long skuId);

    @GetMapping("/product/skusaleattrvalue/skuAttr/{skuId}")
    public List<String> getSkuAttrSale(@PathVariable("skuId") Long skuId);

    @GetMapping("/product/skuinfo/getPrice")
    public R getPrice(@RequestParam("skuId") Long skuId);
}
