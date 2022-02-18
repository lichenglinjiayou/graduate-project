package com.lichenglin.gulimall.coupon.service.impl;

import com.lichenglin.common.to.MemberPrice;
import com.lichenglin.common.to.SkuReductionTo;
import com.lichenglin.gulimall.coupon.entity.SmsMemberPriceEntity;
import com.lichenglin.gulimall.coupon.entity.SmsSkuLadderEntity;
import com.lichenglin.gulimall.coupon.service.SmsMemberPriceService;
import com.lichenglin.gulimall.coupon.service.SmsSkuLadderService;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.lichenglin.common.utils.PageUtils;
import com.lichenglin.common.utils.Query;

import com.lichenglin.gulimall.coupon.dao.SmsSkuFullReductionDao;
import com.lichenglin.gulimall.coupon.entity.SmsSkuFullReductionEntity;
import com.lichenglin.gulimall.coupon.service.SmsSkuFullReductionService;


@Service("smsSkuFullReductionService")
public class SmsSkuFullReductionServiceImpl extends ServiceImpl<SmsSkuFullReductionDao, SmsSkuFullReductionEntity> implements SmsSkuFullReductionService {

    @Autowired
    SmsSkuLadderService smsSkuLadderService;

    @Autowired
    SmsMemberPriceService smsMemberPriceService;


    @Override
    public PageUtils queryPage(Map<String, Object> params) {
        IPage<SmsSkuFullReductionEntity> page = this.page(
                new Query<SmsSkuFullReductionEntity>().getPage(params),
                new QueryWrapper<SmsSkuFullReductionEntity>()
        );

        return new PageUtils(page);
    }

    @Override
    public void saveSkuReduction(SkuReductionTo skuReductionTo) {
        //3 保存满减打折，会员价；
        SmsSkuLadderEntity smsSkuLadderEntity = new SmsSkuLadderEntity();
        smsSkuLadderEntity.setSkuId(skuReductionTo.getSkuId());
        smsSkuLadderEntity.setFullCount(skuReductionTo.getFullCount());
        smsSkuLadderEntity.setDiscount(skuReductionTo.getDiscount());
        smsSkuLadderEntity.setAddOther(skuReductionTo.getCountStatus());
        if(smsSkuLadderEntity.getFullCount() >0){
            smsSkuLadderService.save(smsSkuLadderEntity);
        }

        // full_reduction
        SmsSkuFullReductionEntity smsSkuFullReductionEntity = new SmsSkuFullReductionEntity();
        BeanUtils.copyProperties(skuReductionTo,smsSkuFullReductionEntity);
        if(smsSkuFullReductionEntity.getFullPrice().compareTo(new BigDecimal(0)) == 1){
            this.save(smsSkuFullReductionEntity);
        }

        //sms_user_service
        List<MemberPrice> memberPrice = skuReductionTo.getMemberPrice();
        List<SmsMemberPriceEntity> memberPriceEntities = new ArrayList<>();
        memberPrice.forEach((item)->{
            SmsMemberPriceEntity smsMemberPriceEntity = new SmsMemberPriceEntity();
            smsMemberPriceEntity.setSkuId(skuReductionTo.getSkuId());
            smsMemberPriceEntity.setMemberLevelId(item.getId());
            smsMemberPriceEntity.setMemberLevelName(item.getName());
            smsMemberPriceEntity.setMemberPrice(item.getPrice());
            if(smsMemberPriceEntity.getMemberPrice().compareTo(new BigDecimal(0))==1){
                memberPriceEntities.add(smsMemberPriceEntity);
            }
        });
        smsMemberPriceService.saveBatch(memberPriceEntities);
    }
}