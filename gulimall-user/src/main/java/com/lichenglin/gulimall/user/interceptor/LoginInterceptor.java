package com.lichenglin.gulimall.user.interceptor;

import com.lichenglin.common.constant.AuthServerConstant;
import com.lichenglin.common.vo.UserLoginVo;
import org.springframework.stereotype.Component;
import org.springframework.util.AntPathMatcher;
import org.springframework.web.servlet.HandlerInterceptor;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

@Component
public class LoginInterceptor implements HandlerInterceptor {
    public static ThreadLocal<UserLoginVo> threadLocal = new ThreadLocal<>();
    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        //远程微服务之间的相互调用，免于去登陆页，登陆后在进行执行；
        //对于远程服务的调用，直接放行；
        String requestURI = request.getRequestURI();
        boolean match = new AntPathMatcher().match("/user/**", requestURI);
        if(match){
            return true;
        }
        HttpSession session = request.getSession();
        if(session.getAttribute(AuthServerConstant.LOGIN_USER)==null){
            response.sendRedirect("http://auth.gulimall.com/login.html");
            session.setAttribute("msg","请先登录您的账户");
            return false;
        }else{
            UserLoginVo attribute = (UserLoginVo) session.getAttribute(AuthServerConstant.LOGIN_USER);
            threadLocal.set(attribute);
            return true;
        }
    }
}
