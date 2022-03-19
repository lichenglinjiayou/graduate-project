package com.lichenglin.seckill.controller;

import com.lichenglin.common.utils.R;
import com.lichenglin.seckill.service.SeckillService;
import com.lichenglin.seckill.to.SeckillRedisTo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Controller
public class SeckillController {

    @Autowired
    SeckillService seckillService;


    /**
     * 返回当前时间可以参与的秒杀商品信息
     * @return
     */
    @GetMapping("/getSeckillProducts")
    @ResponseBody
    public R getSeckillProducts(){
       List<SeckillRedisTo> result = seckillService.getSeckillProducts();
        return R.ok().setData(result);
    }


    @GetMapping("/getSkuSecInfo/{skuId}")
    @ResponseBody
    public R getSkuSecInfo(@PathVariable("skuId") Long skuId){
        SeckillRedisTo seckillRedisTo = seckillService.getSkuKillInfo(skuId);
        return R.ok().setData(seckillRedisTo);
    }

    @GetMapping("/killProduct")
    public String handleSecKillRequest(@RequestParam("killId") String killId,
                                  @RequestParam("randomCode") String randomCode,
                                  @RequestParam("num") Integer num,
                                       Model model){
        //限流II：后端继续判断用户是否登录？
        String orderSn =  seckillService.handleSecKillRequest(killId,randomCode,num);
        model.addAttribute("orderSn",orderSn);
        return "success";
    }
}
