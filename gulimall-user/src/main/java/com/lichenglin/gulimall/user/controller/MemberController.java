package com.lichenglin.gulimall.user.controller;

import java.util.Arrays;
import java.util.Map;

import com.lichenglin.common.exception.BizCodeEnum;
import com.lichenglin.gulimall.user.exception.PhoneExistsException;
import com.lichenglin.gulimall.user.exception.UsernameExistsException;
import com.lichenglin.gulimall.user.feign.UserCoupon;
import com.lichenglin.gulimall.user.vo.UserLoginVo;
import com.lichenglin.gulimall.user.vo.UserRegistVo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import com.lichenglin.gulimall.user.entity.MemberEntity;
import com.lichenglin.gulimall.user.service.MemberService;
import com.lichenglin.common.utils.PageUtils;
import com.lichenglin.common.utils.R;



/**
 * 会员
 *
 * @author lichenglin
 * @email 13384966629@163.com
 * @date 2022-02-09 20:33:32
 */
@RestController
@RequestMapping("user/member")
public class MemberController {
    @Autowired
    private MemberService memberService;

    @Autowired
    private UserCoupon userCoupon;

    @RequestMapping("/coupon/list")
    public R userCoupon(){
        MemberEntity memberEntity = new MemberEntity();
        memberEntity.setNickname("lichenglin");
        memberEntity.setCity("西安");
        return R.ok().put("user",memberEntity).put("coupons",userCoupon.userCoupon().get("coupons"));
    }

    @PostMapping("/regist")
    public R regist(@RequestBody UserRegistVo userRegistVo){
        try {
            memberService.regist(userRegistVo);
        } catch (PhoneExistsException e) {
            return R.error(BizCodeEnum.TELEPHONE_EXIST_EXCEPTION.getCode(),BizCodeEnum.TELEPHONE_EXIST_EXCEPTION.getMessage());
        }catch (UsernameExistsException e){
            return R.error(BizCodeEnum.USER_EXIST_EXCEPTION.getCode(),BizCodeEnum.USER_EXIST_EXCEPTION.getMessage());
        }
        return R.ok();
    }

    @PostMapping("/login")
    public R login(@RequestBody UserLoginVo userLoginVo){
        MemberEntity memberEntity = memberService.login(userLoginVo);
        if(memberEntity != null){
            return R.ok().setData(memberEntity);
        }else{
            return R.error(BizCodeEnum.LOGIN_FAILED_EXCEPTION.getCode(),BizCodeEnum.LOGIN_FAILED_EXCEPTION.getMessage());
        }
    }


    /**
     * 列表
     */
    @RequestMapping("/list")
    public R list(@RequestParam Map<String, Object> params){
        PageUtils page = memberService.queryPage(params);

        return R.ok().put("page", page);
    }


    /**
     * 信息
     */
    @RequestMapping("/info/{id}")
    public R info(@PathVariable("id") Long id){
		MemberEntity member = memberService.getById(id);

        return R.ok().put("member", member);
    }

    /**
     * 保存
     */
    @RequestMapping("/save")
    public R save(@RequestBody MemberEntity member){
		memberService.save(member);

        return R.ok();
    }

    /**
     * 修改
     */
    @RequestMapping("/update")
    public R update(@RequestBody MemberEntity member){
		memberService.updateById(member);

        return R.ok();
    }

    /**
     * 删除
     */
    @RequestMapping("/delete")
    public R delete(@RequestBody Long[] ids){
		memberService.removeByIds(Arrays.asList(ids));

        return R.ok();
    }

}
