package com.lichenglin.gulimall.thirdparty.controller;

import com.lichenglin.common.utils.R;
import com.lichenglin.gulimall.thirdparty.component.SmsComponent;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
@RequestMapping("/sms")
public class SmsSendController {

    @Autowired
    SmsComponent smsComponent;
    /**
     * 提供其他微服务调用
     * @param phone
     * @param code
     * @return
     */
    @GetMapping("/sendCode")
    public R sendCode(@RequestParam("phone") String phone, @RequestParam("code") String code){
        smsComponent.sendCode(phone,code);
        return R.ok();
    }
}
