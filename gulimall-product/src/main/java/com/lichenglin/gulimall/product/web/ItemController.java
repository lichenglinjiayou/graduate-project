package com.lichenglin.gulimall.product.web;

import com.lichenglin.gulimall.product.service.SkuInfoService;
import com.lichenglin.gulimall.product.vo.SkuItemVo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

@Controller
public class ItemController {
    @Autowired
    SkuInfoService skuInfoService;
    @GetMapping("/{skuId}.html")
    public String skuItem(@PathVariable("skuId") Long skuId, Model model){
        SkuItemVo skuItemVo = skuInfoService.getSkuInfo(skuId);
        model.addAttribute("item",skuItemVo);
        System.out.println(skuItemVo);
        return "item";
    }
}
