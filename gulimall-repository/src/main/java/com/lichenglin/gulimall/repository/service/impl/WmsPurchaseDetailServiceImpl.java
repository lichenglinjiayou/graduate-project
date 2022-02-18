package com.lichenglin.gulimall.repository.service.impl;

import org.apache.commons.lang.StringUtils;
import org.springframework.stereotype.Service;
import java.util.Map;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.lichenglin.common.utils.PageUtils;
import com.lichenglin.common.utils.Query;

import com.lichenglin.gulimall.repository.dao.WmsPurchaseDetailDao;
import com.lichenglin.gulimall.repository.entity.WmsPurchaseDetailEntity;
import com.lichenglin.gulimall.repository.service.WmsPurchaseDetailService;


@Service("wmsPurchaseDetailService")
public class WmsPurchaseDetailServiceImpl extends ServiceImpl<WmsPurchaseDetailDao, WmsPurchaseDetailEntity> implements WmsPurchaseDetailService {

    @Override
    public PageUtils queryPage(Map<String, Object> params) {
        IPage<WmsPurchaseDetailEntity> page = this.page(
                new Query<WmsPurchaseDetailEntity>().getPage(params),
                new QueryWrapper<WmsPurchaseDetailEntity>()
        );

        return new PageUtils(page);
    }

    @Override
    public PageUtils queryPageByCondition(Map<String, Object> params) {
        QueryWrapper<WmsPurchaseDetailEntity> wrapper = new QueryWrapper<>();
        String key = (String) params.get("key");
        String wareId = (String) params.get("wareId");
        String status = (String) params.get("status");

        if(!StringUtils.isEmpty(key)){
            wrapper.and((w)->{
                w.eq("purchase_id",key).or().eq("sku_id",key);
            });
        }
        if(!StringUtils.isEmpty(wareId)){
            wrapper.eq("ware_id",wareId);
        }
        if(!StringUtils.isEmpty(status)){
            wrapper.eq("status",status);
        }

        IPage<WmsPurchaseDetailEntity> page = this.page(
                new Query<WmsPurchaseDetailEntity>().getPage(params),
                wrapper
        );

        return new PageUtils(page);
    }

}