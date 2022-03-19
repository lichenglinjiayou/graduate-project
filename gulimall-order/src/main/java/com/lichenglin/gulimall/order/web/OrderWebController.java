package com.lichenglin.gulimall.order.web;

import com.lichenglin.gulimall.order.service.OrderService;
import com.lichenglin.gulimall.order.vo.OrderConfirmVo;
import com.lichenglin.gulimall.order.vo.OrderSubmitResponseVo;
import com.lichenglin.gulimall.order.vo.OrderSubmitVo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
public class OrderWebController {

    @Autowired
    OrderService orderService;

    @GetMapping("/toTrade")
    public String toTrade(Model model){
        OrderConfirmVo confirmVo = orderService.confirmOrder();
        model.addAttribute("orderConfirmData",confirmVo);
        return "confirm";
    }

    /**
     * 下单功能：创建订单->验价格->锁库存
     * ->支付选择页->下单失败，重新确认下单信息；
     * @param orderSubmitVo
     * @return
     */
    @PostMapping("/submit")
    public String submitOrder(OrderSubmitVo orderSubmitVo, Model model, RedirectAttributes redirectAttributes){
        OrderSubmitResponseVo submitResponseVo = orderService.submitOrder(orderSubmitVo);
        if(submitResponseVo.getCode() == 0){
            model.addAttribute("submitOrderResponse",submitResponseVo);
            return "pay";
        }
        if(submitResponseVo.getCode() == 1){
            redirectAttributes.addFlashAttribute("msg","订单信息过期，请重新提交");
        }else if(submitResponseVo.getCode() == 2){
            redirectAttributes.addFlashAttribute("msg","订单商品价格发生变化，请慎重考虑");
        }else{
            redirectAttributes.addFlashAttribute("msg","商品库存不足");
        }
        return "redirect:http://order.gulimall.com/toTrade";
    }
}
