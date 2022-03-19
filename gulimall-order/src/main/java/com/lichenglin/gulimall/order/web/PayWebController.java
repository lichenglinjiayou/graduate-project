package com.lichenglin.gulimall.order.web;

import com.alipay.api.AlipayApiException;
import com.lichenglin.gulimall.order.config.AlipayTemplate;
import com.lichenglin.gulimall.order.service.OrderService;
import com.lichenglin.gulimall.order.vo.PayVo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

@Controller
public class PayWebController {
    @Autowired
    AlipayTemplate alipayTemplate;
    @Autowired
    OrderService orderService;

    /**
     *
     * 支付宝支付成功后，支付页让浏览器展示；
     * 支付成功后，跳转到用户的订单列表页；
     */
    @ResponseBody
    @GetMapping(value = "/payOrder",produces = "text/html")
    public String payOrder(@RequestParam("orderSn") String orderSn) throws AlipayApiException {
        PayVo entity = orderService.getOrderPayInfo(orderSn);
        //支付宝返回的是一个页面，因此可以将该页面直接返回；
        String pay = alipayTemplate.pay(entity);
        return pay;
    }
}
