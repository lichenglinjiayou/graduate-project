package com.lichenglin.cart.interceptor;

import com.lichenglin.cart.vo.UserInfoVo;
import com.lichenglin.common.constant.AuthServerConstant;
import com.lichenglin.common.constant.CartConstant;
import com.lichenglin.common.vo.UserLoginVo;
import org.apache.commons.lang.StringUtils;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;
import org.springframework.web.servlet.ModelAndView;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.util.UUID;

/*
    拦截器的作用:判断登录状态，并封装传递给controller处理器；
 */

@Component
public class CartInterceptor implements HandlerInterceptor {
    public static ThreadLocal<UserInfoVo> threadLocal = new ThreadLocal<>();
    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        UserInfoVo userInfoVo = new UserInfoVo();
        HttpSession session = request.getSession();
        UserLoginVo attribute = (UserLoginVo) session.getAttribute(AuthServerConstant.LOGIN_USER);
        if(attribute != null){
            userInfoVo.setUserId(attribute.getId());
        }
        Cookie[] cookies = request.getCookies();
        if(cookies != null && cookies.length > 0){
            for (Cookie cookie : cookies) {
                String name = cookie.getName();
                if(name.equals(CartConstant.TEMP_USER_COOKIE)){
                    userInfoVo.setUserKey(cookie.getValue());
                    userInfoVo.setFlag(true);
                }
            }
        }

        //分配临时用户
        if(StringUtils.isEmpty(userInfoVo.getUserKey())){
            String uuid = UUID.randomUUID().toString().replace("-","");
            userInfoVo.setUserKey(uuid);
        }

        //使用threadLocal在同一个线程中共享数据;
        threadLocal.set(userInfoVo);
        return true;
    }

    @Override
    public void postHandle(HttpServletRequest request, HttpServletResponse response, Object handler, ModelAndView modelAndView) throws Exception {
        UserInfoVo userInfoVo = threadLocal.get();
        if(!userInfoVo.getFlag()){
            Cookie cookie = new Cookie(CartConstant.TEMP_USER_COOKIE,threadLocal.get().getUserKey());
            cookie.setDomain("gulimall.com");
            cookie.setMaxAge(CartConstant.USER_COOKIE_TIMEOUT);
            response.addCookie(cookie);
        }
    }

    @Override
    public void afterCompletion(HttpServletRequest request, HttpServletResponse response, Object handler, Exception ex) throws Exception {

    }
}
