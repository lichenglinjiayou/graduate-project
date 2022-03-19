package com.lichenglin.seckill.scheduled;

import com.lichenglin.seckill.service.SeckillService;
import lombok.extern.slf4j.Slf4j;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Service;

import java.util.concurrent.TimeUnit;


@Slf4j
@Service
/**
 * 秒杀商品定时上架；
 *  每天凌晨上架最近3天秒杀的商品；
 */
public class SecSkillSkuScheduled {

    @Autowired
    SeckillService seckillService;
    @Autowired
    RedissonClient redissonClient;

    private static  final String UPLOAD_LOCK="seckill:upload:lock";
    /**
     * 幂等性处理
     */
    @Scheduled(cron = "*/20 * * * * ?")
    public void uploadSecSkillSkuLatest3Days(){
        log.info("上架秒杀商品信息");
        //1. 使用redis添加分布式锁，保证每次只要一个服务器执行商品上架的任务；
        // 防止所有服务器一起通过幂等性判断，完成商品上架
        RLock lock = redissonClient.getLock(UPLOAD_LOCK);
        lock.lock(10, TimeUnit.SECONDS);
        try {
            //2. 服务器内的幂等性
            seckillService.uploadCommodity();
        } catch (Exception e) {
            e.printStackTrace();
        }finally {
            lock.unlock();
        }
    }
}
