package com.lichenglin.gulimall.order.listener;

import com.alipay.api.AlipayApiException;
import com.alipay.api.AlipayConfig;
import com.alipay.api.internal.util.AlipaySignature;
import com.lichenglin.gulimall.order.config.AlipayTemplate;
import com.lichenglin.gulimall.order.service.OrderService;
import com.lichenglin.gulimall.order.vo.PayAsyncVo;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletRequest;
import java.io.UnsupportedEncodingException;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

@RestController
@Slf4j
public class OrderPayedListener {
    @Autowired
    OrderService orderService;
    @Autowired
    AlipayTemplate alipayTemplate;
    @PostMapping("/payed/notify")
    public String alipay(PayAsyncVo vo,HttpServletRequest request) throws UnsupportedEncodingException, AlipayApiException {
        Map<String, String[]> parameterMap = request.getParameterMap();
        //收到支付宝的异步通知，告知订单支付成功，返回success;
        //支付宝则不在进行间隔通知；
        //返回之前进行验签操作,防止数据被篡改或伪造；
        Map<String,String> params = new HashMap<>();
        for(Iterator<String> iter = parameterMap.keySet().iterator();iter.hasNext();){
            String name = iter.next();
            String[] values = parameterMap.get(name);
            String valueStr = "";
            for(int i = 0;i < values.length;i++){
                valueStr = (i == values.length - 1) ? valueStr + values[i] : valueStr + values[i] + ",";
            }
//            valueStr = new String(valueStr.getBytes("ISO-8859-1"), "UTF-8");
            params.put(name,valueStr);
        }
        boolean signVerified = AlipaySignature.rsaCheckV1(params, alipayTemplate.getAlipay_public_key(),alipayTemplate.getCharset(),alipayTemplate.getSign_type());
        if(signVerified){
            log.info("签名验证成功");
            String result = orderService.handleOutcome(vo);
            return result;
        }else{
            log.info("签名验证失败");
            return "error";
        }
    }

}
