package com.lichenglin.gulimall.coupon.service.impl;

import com.lichenglin.gulimall.coupon.entity.SmsSeckillSkuRelationEntity;
import com.lichenglin.gulimall.coupon.service.SmsSeckillSkuRelationService;
import org.apache.tomcat.jni.Local;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.text.SimpleDateFormat;
import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.Date;
import java.util.List;
import java.util.Map;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.lichenglin.common.utils.PageUtils;
import com.lichenglin.common.utils.Query;

import com.lichenglin.gulimall.coupon.dao.SmsSeckillSessionDao;
import com.lichenglin.gulimall.coupon.entity.SmsSeckillSessionEntity;
import com.lichenglin.gulimall.coupon.service.SmsSeckillSessionService;


@Service("smsSeckillSessionService")
public class SmsSeckillSessionServiceImpl extends ServiceImpl<SmsSeckillSessionDao, SmsSeckillSessionEntity> implements SmsSeckillSessionService {

    @Autowired
    private SmsSeckillSkuRelationService smsSeckillSkuRelationService;


    @Override
    public PageUtils queryPage(Map<String, Object> params) {
        IPage<SmsSeckillSessionEntity> page = this.page(
                new Query<SmsSeckillSessionEntity>().getPage(params),
                new QueryWrapper<SmsSeckillSessionEntity>()
        );

        return new PageUtils(page);
    }

    @Override
    public List<SmsSeckillSessionEntity> getSessionLatest3Days() {
        //计算起始时间和结束时间:java8的localDate \ localTime \ localDateTime
        List<SmsSeckillSessionEntity> entities = this.list(new QueryWrapper<SmsSeckillSessionEntity>().between("start_time", getStartTime(), getEndTime()));
        if(entities != null && entities.size() > 0){
            entities.forEach((item)->{
                Long id = item.getId();
                List<SmsSeckillSkuRelationEntity> allRelations = smsSeckillSkuRelationService.list(new QueryWrapper<SmsSeckillSkuRelationEntity>().eq("promotion_session_id", id));
                item.setRelationEntities(allRelations);
            });
            return entities;
        }
        return null;
    }

    private String getStartTime(){
        LocalDate now = LocalDate.now();
        LocalTime min = LocalTime.MIN;
        LocalDateTime startTime = LocalDateTime.of(now, min);

        String start = startTime.format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
        return start;
    }

    private String getEndTime(){
        LocalDate now = LocalDate.now();
        LocalDate end = now.plusDays(2);
        LocalTime max = LocalTime.MAX;
        LocalDateTime endTime = LocalDateTime.of(end, max);

        String ends = endTime.format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
        return ends;
    }

}