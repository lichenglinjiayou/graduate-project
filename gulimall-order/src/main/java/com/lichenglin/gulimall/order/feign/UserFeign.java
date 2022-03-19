package com.lichenglin.gulimall.order.feign;

import com.lichenglin.gulimall.order.vo.MemberAddressVo;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

import java.util.List;

@FeignClient("gulimall-user")
public interface UserFeign {

    @GetMapping("/user/memberreceiveaddress/{userId}/address")
    List<MemberAddressVo> getAddress(@PathVariable("userId") Long memberId);
}
