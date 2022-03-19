package com.lichenglin.ssoserver.controller;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.ToString;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.CookieValue;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.io.Serializable;
import java.util.UUID;

@Controller
public class LoginController {

        @Autowired
        StringRedisTemplate stringRedisTemplate;

        @PostMapping("/doLogin")
        public String login(String username, String password, String url, HttpServletResponse response){
            if(!StringUtils.isEmpty(username) && !StringUtils.isEmpty(password)){

                String uuid = UUID.randomUUID().toString();
                stringRedisTemplate.opsForValue().set(uuid,new User(username,password).toString());

                // 给当前系统增加一个cookie,解决单点登录问题；
                Cookie cookie = new Cookie("sso_token",uuid);
                response.addCookie(cookie);
                return "redirect:"+url+"?token="+uuid;
            }
            return "login";
        }


        @GetMapping("/login.html")
        public String goToLogin(@RequestParam("redirect_url") String url, Model model,
                                @CookieValue(value = "sso_token",required = false) String token){
            if(token != null){
                return "redirect:"+url+"?token="+token;
            }
            model.addAttribute("url",url);
            return "login";
        }
}

@Data
@AllArgsConstructor
@ToString
class User {
    public String username;
    public String password;
}

