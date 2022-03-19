package com.lichenglin.gulimall.user.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.lichenglin.common.utils.PageUtils;
import com.lichenglin.gulimall.user.entity.MemberEntity;
import com.lichenglin.gulimall.user.exception.PhoneExistsException;
import com.lichenglin.gulimall.user.exception.UsernameExistsException;
import com.lichenglin.gulimall.user.vo.UserLoginVo;
import com.lichenglin.gulimall.user.vo.UserRegistVo;

import java.util.Map;

/**
 * 会员
 *
 * @author lichenglin
 * @email 13384966629@163.com
 * @date 2022-02-09 20:33:32
 */
public interface MemberService extends IService<MemberEntity> {

    PageUtils queryPage(Map<String, Object> params);

    void regist(UserRegistVo userRegistVo);

    void checkTelephone(String phone) throws PhoneExistsException;

    void checkUsername(String username) throws UsernameExistsException;

    MemberEntity login(UserLoginVo userLoginVo);
}

