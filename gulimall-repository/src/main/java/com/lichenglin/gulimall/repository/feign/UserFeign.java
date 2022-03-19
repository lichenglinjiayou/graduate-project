package com.lichenglin.gulimall.repository.feign;

import com.lichenglin.common.utils.R;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;

@FeignClient("gulimall-user")
public interface UserFeign {
    @RequestMapping("/user/memberreceiveaddress/info/{id}")
    public R info(@PathVariable("id") Long id);
}
