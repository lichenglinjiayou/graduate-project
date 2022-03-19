package com.lichenglin.gulimall.user.service.impl;

import com.lichenglin.gulimall.user.entity.MemberLevelEntity;
import com.lichenglin.gulimall.user.exception.PhoneExistsException;
import com.lichenglin.gulimall.user.exception.UsernameExistsException;
import com.lichenglin.gulimall.user.service.MemberLevelService;
import com.lichenglin.gulimall.user.vo.UserLoginVo;
import com.lichenglin.gulimall.user.vo.UserRegistVo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.lichenglin.common.utils.PageUtils;
import com.lichenglin.common.utils.Query;

import com.lichenglin.gulimall.user.dao.MemberDao;
import com.lichenglin.gulimall.user.entity.MemberEntity;
import com.lichenglin.gulimall.user.service.MemberService;


@Service("memberService")
public class MemberServiceImpl extends ServiceImpl<MemberDao, MemberEntity> implements MemberService {

    @Autowired
    MemberLevelService memberLevelService;
    @Override
    public PageUtils queryPage(Map<String, Object> params) {
        IPage<MemberEntity> page = this.page(
                new Query<MemberEntity>().getPage(params),
                new QueryWrapper<MemberEntity>()
        );

        return new PageUtils(page);
    }

    @Override
    public void regist(UserRegistVo userRegistVo) {
        MemberEntity memberEntity = new MemberEntity();
        checkUsername(userRegistVo.getUsername());
        checkTelephone(userRegistVo.getTelephone());
        memberEntity.setUsername(userRegistVo.getUsername());
        memberEntity.setMobile(userRegistVo.getTelephone());
        //密码进行md5加密存储
        /**
         * md5 加密仍然存在被暴力破解的可能，因此需要在进行盐值加密；
         */
        BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();
        String encode = encoder.encode(userRegistVo.getPassword());
        memberEntity.setPassword(encode);
        //设置默认等级
        List<MemberLevelEntity> default_status = memberLevelService.list(new QueryWrapper<MemberLevelEntity>().eq("default_status", 1));
        memberEntity.setLevelId((long)default_status.get(0).getDefaultStatus());

        memberEntity.setNickname(userRegistVo.getUsername());

        this.baseMapper.insert(memberEntity);
    }

    @Override
    public void checkTelephone(String phone) throws PhoneExistsException{
        List<MemberEntity> mobile = this.baseMapper.selectList(new QueryWrapper<MemberEntity>().eq("mobile", phone));
        if(mobile.size() > 0){
            throw new PhoneExistsException();
        }
    }

    @Override
    public void checkUsername(String username) throws UsernameExistsException{
        Integer count = this.baseMapper.selectCount(new QueryWrapper<MemberEntity>().eq("username", username));
        if(count > 0){
            throw new UsernameExistsException();
        }
    }

    @Override
    public MemberEntity login(UserLoginVo userLoginVo) {

        List<MemberEntity> list = this.list(new QueryWrapper<MemberEntity>().eq("username", userLoginVo.getLoginAccount()).or().eq("mobile", userLoginVo.getLoginAccount()));
        if(list.get(0) == null){
            return null;
        }
        String password = list.get(0).getPassword();
        BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();
        boolean matches = encoder.matches(userLoginVo.getPassword(), password);
        if(matches){
            return list.get(0);
        }else{
            return null;
        }
    }

}