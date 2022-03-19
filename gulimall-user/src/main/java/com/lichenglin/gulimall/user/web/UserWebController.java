package com.lichenglin.gulimall.user.web;

import com.alibaba.fastjson.JSON;
import com.lichenglin.common.utils.R;
import com.lichenglin.gulimall.user.feign.OrderFeign;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.HashMap;
import java.util.Map;

@Controller
public class UserWebController {

    @Autowired
    OrderFeign orderFeign;

    @GetMapping("/userOrderList.html")
    public String userOrderList(@RequestParam(value = "pageNum",defaultValue = "1") Integer pageNum,
                                Model model){
        //查出当前用户登录的所有订单列表数据
        Map<String,Object> map = new HashMap<>();
        map.put("page",pageNum+"");
        R r = orderFeign.getItemList(map);
        System.out.println(JSON.toJSONString(r));
        model.addAttribute("orders",r);
        return "userOrderList";
    }
}
