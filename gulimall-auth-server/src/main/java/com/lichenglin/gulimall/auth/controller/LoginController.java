package com.lichenglin.gulimall.auth.controller;

import com.alibaba.fastjson.TypeReference;
import com.lichenglin.common.constant.AuthServerConstant;
import com.lichenglin.common.exception.BizCodeEnum;
import com.lichenglin.common.utils.R;
import com.lichenglin.common.vo.UserLoginVo;
import com.lichenglin.gulimall.auth.feign.ThirdPartyFeign;
import com.lichenglin.gulimall.auth.feign.UserFeign;
import com.lichenglin.gulimall.auth.vo.UserRegistVo;
import org.apache.catalina.session.StandardSession;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import javax.servlet.http.HttpSession;
import javax.validation.Valid;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

@Controller
public class LoginController {

    @Autowired
    StringRedisTemplate stringRedisTemplate;
    @Autowired
    ThirdPartyFeign thirdPartyFeign;

    @Autowired
    UserFeign userFeign;

    @GetMapping("/sms/sendcode")
    @ResponseBody
    public R sendCode(@RequestParam("phone") String phone) {
        String redisCode = stringRedisTemplate.opsForValue().get(AuthServerConstant.SMS_CODE_PREFIX + phone);
        if (redisCode != null) {
            String[] s = redisCode.split("_");
            Long time = Long.parseLong(s[1]);
            if (System.currentTimeMillis() - time < 60000) {
                //60s内
                return R.error(BizCodeEnum.VALIDATE_SMS_CODE_EXCEPTION.getCode(), BizCodeEnum.VALIDATE_SMS_CODE_EXCEPTION.getMessage());
            }
        }
        String substring = UUID.randomUUID().toString().substring(0, 5);
        String code = substring + "_" + System.currentTimeMillis();
        // 使用redis远程缓存验证码
        stringRedisTemplate.opsForValue().set(AuthServerConstant.SMS_CODE_PREFIX + phone, code, 10, TimeUnit.MINUTES);
        //防止同一个手机号60s内再次发送验证码

        thirdPartyFeign.sendCode(phone, substring);
        return R.ok();
    }

    /**
     *
     * @param registVo
     * @param result
     * @param redirectAttributes //重定向携带数据,利用session原理，将数据放入到session中，
     *                           当跳到下一个页面取出数据后，session中的数据会被删除；
     *                           TODO:分布式的session问题？
     * @return
     */
    @PostMapping("/regist")
    public String regist(@Valid UserRegistVo registVo, BindingResult result, RedirectAttributes redirectAttributes){
        //注册成功回到登录页
        if(result.hasErrors()){
            Map<String,String> errors = new HashMap<>();
            result.getFieldErrors().forEach((item)->{
                String field = item.getField();
                String message = item.getDefaultMessage();
                errors.put(field,message);
            });
            //信息校验出错，转发到注册页
            redirectAttributes.addFlashAttribute("errors",errors);
            return "redirect:http://auth.gulimall.com/register.html";
        }
        String code = registVo.getCode();
        String s = stringRedisTemplate.opsForValue().get(AuthServerConstant.SMS_CODE_PREFIX + registVo.getTelephone());
        if(StringUtils.isEmpty(s)){
            //1.如果无法从redis中获取验证码，则验证码过期，转发到注册页
            Map<String,String> map = new HashMap<>();
            map.put("code","验证码错误");
            redirectAttributes.addFlashAttribute("errors",map);
            return "redirect:http://auth.gulimall.com/register.html";
        }else{
            //2.验证码正确
            String[] s1 = s.split("_");
            if(s1[0].equals(code)){
                //删除验证码：令牌机制
                stringRedisTemplate.delete(AuthServerConstant.SMS_CODE_PREFIX + registVo.getTelephone());
                R regist = userFeign.regist(registVo);
                if(regist.getCode() == 0){
                    //注册成功
                    return  "redirect:http://auth.gulimall.com/login.html";
                }else{
                    //注册失败
                    Map<String,String> errors = new HashMap<>();
                    errors.put("message",regist.getData("message",new TypeReference<String>(){}));
                    redirectAttributes.addFlashAttribute("errors",errors);
                    return "redirect:http://auth.gulimall.com/register.html";
                }
            }else{
                Map<String,String> map = new HashMap<>();
                map.put("code","验证码错误");
                redirectAttributes.addFlashAttribute("errors",map);
                return "redirect:http://auth.gulimall.com/register.html";
            }
        }
    }


    @PostMapping("/login")
    public String login(UserLoginVo userLoginVo, RedirectAttributes redirectAttributes, HttpSession session){
        R login = userFeign.login(userLoginVo);
        if(login.getCode() == 0){
            UserLoginVo data = login.getData(new TypeReference<UserLoginVo>() {});
            userLoginVo.setId(data.getId());
            session.setAttribute(AuthServerConstant.LOGIN_USER,userLoginVo);
            //1. session作用域不能解决跨域名共享的问题；
            //2. 使用JSON序列化方式序列化对象到redis;
            return "redirect:http://gulimall.com";
        }else{
            Map<String,String> errors = new HashMap<>();
            errors.put("message",login.getData("message",new TypeReference<String>(){}));
            redirectAttributes.addFlashAttribute("errors",errors);
            return "redirect:http://auth.gulimall.com/login.html";
        }
    }

    @GetMapping("/login.html")
    public String loginPage(HttpSession session){
        Object attribute = session.getAttribute(AuthServerConstant.LOGIN_USER);
        if(attribute == null){
            return "login";
        }else{
            return "redirect:http://gulimall.com";
        }
    }
}
