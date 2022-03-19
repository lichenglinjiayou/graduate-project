package com.lichenglin.gulimall.auth.feign;

import com.lichenglin.common.utils.R;
import com.lichenglin.common.vo.UserLoginVo;
import com.lichenglin.gulimall.auth.vo.UserRegistVo;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

@FeignClient("gulimall-user")
public interface UserFeign {
    @PostMapping("/user/member/regist")
    public R regist(@RequestBody UserRegistVo userRegistVo);

    @PostMapping("/user/member/login")
    public R login(@RequestBody UserLoginVo userLoginVo);
}
