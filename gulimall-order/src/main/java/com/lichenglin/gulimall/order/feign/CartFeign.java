package com.lichenglin.gulimall.order.feign;

import com.lichenglin.gulimall.order.vo.OrderItemVo;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;

import java.util.List;

@FeignClient("gulimall-cart")
public interface CartFeign {
    @GetMapping("/cartItems")
    List<OrderItemVo> getCartItems();
}
