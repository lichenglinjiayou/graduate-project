package com.lichenglin.gulimall.coupon.service.impl;

import org.apache.commons.lang.StringUtils;
import org.springframework.stereotype.Service;
import java.util.Map;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.lichenglin.common.utils.PageUtils;
import com.lichenglin.common.utils.Query;

import com.lichenglin.gulimall.coupon.dao.SmsSeckillSkuRelationDao;
import com.lichenglin.gulimall.coupon.entity.SmsSeckillSkuRelationEntity;
import com.lichenglin.gulimall.coupon.service.SmsSeckillSkuRelationService;


@Service("smsSeckillSkuRelationService")
public class SmsSeckillSkuRelationServiceImpl extends ServiceImpl<SmsSeckillSkuRelationDao, SmsSeckillSkuRelationEntity> implements SmsSeckillSkuRelationService {


    @Override
    public PageUtils queryPage(Map<String, Object> params) {
        QueryWrapper<SmsSeckillSkuRelationEntity> queryWrapper = new QueryWrapper<SmsSeckillSkuRelationEntity>();
        //场次ID不为空
        if(!StringUtils.isEmpty((String)params.get("promotionSessionId"))){
            queryWrapper.eq("promotion_session_id",params.get("promotionSessionId"));
        }
        IPage<SmsSeckillSkuRelationEntity> page = this.page(
                new Query<SmsSeckillSkuRelationEntity>().getPage(params),
                queryWrapper
        );

        return new PageUtils(page);
    }

}