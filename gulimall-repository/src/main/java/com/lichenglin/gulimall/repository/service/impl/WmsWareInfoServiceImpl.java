package com.lichenglin.gulimall.repository.service.impl;

import com.alibaba.fastjson.TypeReference;
import com.lichenglin.common.utils.R;
import com.lichenglin.gulimall.repository.feign.UserFeign;
import com.lichenglin.gulimall.repository.vo.FareResponseVo;
import com.lichenglin.gulimall.repository.vo.MemberAddressVo;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.lichenglin.common.utils.PageUtils;
import com.lichenglin.common.utils.Query;

import com.lichenglin.gulimall.repository.dao.WmsWareInfoDao;
import com.lichenglin.gulimall.repository.entity.WmsWareInfoEntity;
import com.lichenglin.gulimall.repository.service.WmsWareInfoService;


@Service("wmsWareInfoService")
public class WmsWareInfoServiceImpl extends ServiceImpl<WmsWareInfoDao, WmsWareInfoEntity> implements WmsWareInfoService {

    @Autowired
    UserFeign userFeign;
    @Autowired
    WmsWareInfoService wmsWareInfoService;
    @Override
    public PageUtils queryPage(Map<String, Object> params) {
        IPage<WmsWareInfoEntity> page = this.page(
                new Query<WmsWareInfoEntity>().getPage(params),
                new QueryWrapper<WmsWareInfoEntity>()
        );

        return new PageUtils(page);
    }

    @Override
    public PageUtils queryPageByCondition(Map<String, Object> params) {
        QueryWrapper<WmsWareInfoEntity> wrapper = new QueryWrapper<>();
        String key = (String) params.get("key");
        if(!StringUtils.isEmpty(key)){
            wrapper.eq("id",key).or().eq("areacode",key).or()
                    .like("name",key).or().like("address",key);
        }
        IPage<WmsWareInfoEntity> page = this.page(
                new Query<WmsWareInfoEntity>().getPage(params),
                wrapper
        );
        return new PageUtils(page);
    }

    @Override
    public FareResponseVo getAddressFare(Long addrId) {
        FareResponseVo fareResponseVo = new FareResponseVo();
        R info = userFeign.info(addrId);
        MemberAddressVo data = info.getData("memberReceiveAddress",new TypeReference<MemberAddressVo>() {
        });
        fareResponseVo.setMemberAddressVo(data);
        if(data != null){
            List<WmsWareInfoEntity> list = wmsWareInfoService.list();
            for (WmsWareInfoEntity wmsWareInfoEntity : list) {
                String address = wmsWareInfoEntity.getAddress();
                if(address.contains(data.getProvince()) || address.contains(data.getCity())){
                    fareResponseVo.setFare(new BigDecimal(6));
                    return  fareResponseVo;
                }
            }
        }
        fareResponseVo.setFare(new BigDecimal(15));
        return fareResponseVo;
    }

}