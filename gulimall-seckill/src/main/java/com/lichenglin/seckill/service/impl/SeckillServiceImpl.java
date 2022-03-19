package com.lichenglin.seckill.service.impl;

import com.alibaba.csp.sentinel.Entry;
import com.alibaba.csp.sentinel.SphU;
import com.alibaba.csp.sentinel.annotation.SentinelResource;
import com.alibaba.csp.sentinel.slots.block.BlockException;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.TypeReference;
import com.baomidou.mybatisplus.core.toolkit.IdWorker;
import com.lichenglin.common.to.SeckillOrderTo;
import com.lichenglin.common.utils.R;
import com.lichenglin.seckill.feige.CouponFeign;
import com.lichenglin.seckill.feige.ProductFeign;
import com.lichenglin.seckill.interceptor.LoginInterceptor;
import com.lichenglin.seckill.service.SeckillService;
import com.lichenglin.seckill.to.SeckillRedisTo;
import com.lichenglin.seckill.vo.SecKillSkuVo;
import com.lichenglin.seckill.vo.SeckillSessionRelationVo;
import com.lichenglin.seckill.vo.SkuInfoVo;
import lombok.extern.slf4j.Slf4j;
import org.redisson.api.RSemaphore;
import org.redisson.api.RedissonClient;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.BoundHashOperations;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import javax.swing.text.html.parser.Entity;
import java.math.BigDecimal;
import java.util.*;
import java.util.concurrent.TimeUnit;

@Slf4j
@Service
public class SeckillServiceImpl implements SeckillService {

    @Autowired
    CouponFeign couponFeign;
    @Autowired
    StringRedisTemplate stringRedisTemplate;

    private static final String SESSIONS_PREFIX = "seckill:sessions:";
    private static final String SKU_PREFIX = "seckill:skus:";
    private static final String SKU_SEMAPHORE = "seckill:stock:";//+ shan商品随机码，根据随机码扣除商品的库存；
    @Autowired
    ProductFeign productFeign;
    @Autowired
    RedissonClient redissonClient;
    @Autowired
    RabbitTemplate rabbitTemplate;

    @Override
    public void uploadCommodity() {
        //1. 最近三天哪些活动需要参与秒杀

        R r = couponFeign.getSessionLatest3Days();
        if(r.getCode() == 0){
            //上架商品
            List<SeckillSessionRelationVo> data = r.getData(new TypeReference<List<SeckillSessionRelationVo>>() {
            });
            //1. 缓存活动信息；
            saveSessionInfo(data);
            //2. 缓存活动关联的商品信息;
            saveSkuInfo(data);
        }
    }

    /**
     * 获取sku商品的秒杀信息
     * @return
     */
    @Override
    public SeckillRedisTo getSkuKillInfo(Long skuId) {
        BoundHashOperations<String, String, String> hashOps = stringRedisTemplate.boundHashOps(SKU_PREFIX);
        Set<String> keys = hashOps.keys();
        if(keys != null && keys.size() > 0){
            for (String key : keys) {
                String[] s = key.split("_");
                if(s[1].equals(skuId.toString())){
                    String s1 = hashOps.get(key);
                    SeckillRedisTo seckillRedisTo = JSON.parseObject(s1, SeckillRedisTo.class);
                    Long startTime = seckillRedisTo.getStartTime();
                    Long endTime = seckillRedisTo.getEndTime();
                    Long curTime = new Date().getTime();
                    if(curTime < startTime || curTime > endTime){
                        seckillRedisTo.setRandomCode("");
                    }
                    return seckillRedisTo;
                }
            }
        }
        return  null;
    }
    public List<SeckillRedisTo> blockHandler(BlockException e){
        log.error("方法已经被限流，{}",e.getMessage());
        return null;
    }

    /**
     * blockHandler - 针对指定方法进行降级处理；
     * fallback - 针对所有异常；
     * @return
     */
    @SentinelResource(value = "getSeckillProductsLimit",blockHandler = "blockHandler")
    @Override
    public List<SeckillRedisTo> getSeckillProducts() {

        List<SeckillRedisTo> result = new ArrayList<>();
        try(Entry entry = SphU.entry("secKillProducts")){
            //1,确定当前时间属于哪个秒杀场次；
            long time = new Date().getTime();
            // 查出所有的场次数据
            Set<String> keys = stringRedisTemplate.keys(SESSIONS_PREFIX + "*");
            if(keys != null && keys.size() > 0){
                for (String item : keys) {
                    //0 - seckill ; 1 - sessions ; 2 - 1647597600000_1647601200000
                    String[] split = item.split(":");
                    //0 - 1647597600000; 1 - 1647601200000
                    String[] s = split[2].split("_");
                    long start = Long.parseLong(s[0]);
                    long end = Long.parseLong(s[1]);
                    if(time >= start && time <= end){
                        List<String> range = stringRedisTemplate.opsForList().range(item, -100, 100);
                        BoundHashOperations<String, String, String> hashOps = stringRedisTemplate.boundHashOps(SKU_PREFIX);
                        List<String> objects = hashOps.multiGet(range);
                        if(objects != null && objects.size() > 0){
                            objects.forEach(entity->{
                                SeckillRedisTo seckillRedisTo = JSON.parseObject(entity.toString(), SeckillRedisTo.class);
                                result.add(seckillRedisTo);
                            });
                        }
                        break;
                    }
                }
            }
        }catch (BlockException e){
            log.error("资源被限流，{}",e.getMessage());
        }
        return result;
    }

    //TODO:1.上架商品过期时间；2.收货地址补充；3.秒杀的库存锁定和剩余商品回收库存处理
    @Override
    public String handleSecKillRequest(String killId, String randomCode, Integer num) {
        /*
            对于秒杀的请求，直接放到MQ消息队列中，有订单服务进行监听处理消息，而秒杀微服务直接处理下一个任务：准备订单信息；
         */
        //1. 获取当前秒杀商品的详细信息；
        BoundHashOperations<String, String, String> hashOps = stringRedisTemplate.boundHashOps(SKU_PREFIX);
        String s = hashOps.get(killId);
        // 1.1 对获取的信息进行判断
        if(s == null){
            return  null;
        }else{
            //1.2 转换数据类型；
            SeckillRedisTo seckillRedisTo = JSON.parseObject(s, SeckillRedisTo.class);
            //1.3 校验合法性
            //1.3.1 校验秒杀时间合法性；
            Long startTime = seckillRedisTo.getStartTime();
            Long endTime = seckillRedisTo.getEndTime();
            long curTime = new Date().getTime();
            if(curTime < startTime || curTime > endTime){
                return  null;
            }else{
                //1.3.2 校验随机码的正确性
                String randomCode1 = seckillRedisTo.getRandomCode();
                if(randomCode1.equals(randomCode)){
                    //1.3.3 校验商品数量的有效性
                    BigDecimal seckillLimit = seckillRedisTo.getSeckillLimit();
                    if(num <= seckillLimit.intValue()){
                        //1.3.4 校验用户是否已经购买过-幂等性处理（redis中存储：userId_sessionId_skuId 表示用户是否已经购买过该商品）
                        //占位成功，则之前没有买过该商品，否则该用户已经买过该商品；
                        String key = LoginInterceptor.threadLocal.get().getId()+"_"+killId;
                        //redis中超时时间设置，为秒杀活动的持续时间
                        Boolean result = stringRedisTemplate.opsForValue().setIfAbsent(key, "buy", seckillRedisTo.getEndTime() - seckillRedisTo.getStartTime(), TimeUnit.MILLISECONDS);
                        if(result){
                            //1.3.5 分布式信号量操作；
                            RSemaphore semaphore = redissonClient.getSemaphore(SKU_SEMAPHORE + randomCode);
                            try {
                                //1.4 尝试获取信号量
                                boolean b = semaphore.tryAcquire(num, 200, TimeUnit.MILLISECONDS);
                                if(b == false){
                                    return null;
                                }else {
                                    //1.5 快速下单
                                    //将当前时间作为订单号，进行返回；
                                    String timeId = IdWorker.getTimeId();
                                    SeckillOrderTo seckillOrderTo = new SeckillOrderTo();
                                    seckillOrderTo.setOrderSn(timeId);
                                    seckillOrderTo.setPromotionSessionId(seckillRedisTo.getPromotionSessionId());
                                    seckillOrderTo.setSkuId(seckillRedisTo.getSkuId());
                                    seckillOrderTo.setSeckillCount(new BigDecimal(num));
                                    seckillOrderTo.setSeckillPrice(seckillRedisTo.getSeckillPrice());
                                    seckillOrderTo.setUserId(LoginInterceptor.threadLocal.get().getId());
                                    rabbitTemplate.convertAndSend("order-event-exchange", "order.seckill.order", seckillOrderTo);
                                    return timeId;
                                }
                            } catch (InterruptedException e) {
                                return null;
                            }
                        }else{
                            return null;
                        }
                    }else{
                        return null;
                    }
                }else{
                    return null;
                }
            }
        }
    }

    private void saveSessionInfo( List<SeckillSessionRelationVo> data){
        data.forEach((item)->{
            Long startTime = item.getStartTime().getTime();
            Long endTime = item.getEndTime().getTime();
            String redis_key = SESSIONS_PREFIX +  startTime + "_" + endTime;
            //缓存活动信息，key = 固定格式； value = skuId
            //没有key，即商品还未上架，则执行上架操作；
            if(!stringRedisTemplate.hasKey(redis_key)){
                List<String> skuIds  = new ArrayList<>();
                List<SecKillSkuVo> relationEntities = item.getRelationEntities();
                relationEntities.forEach((entity)->{
                    Long skuId = entity.getSkuId();
                    skuIds.add(entity.getPromotionSessionId().toString()+"_"+skuId.toString());
                });
                stringRedisTemplate.opsForList().leftPushAll(redis_key,skuIds);
                stringRedisTemplate.expire(redis_key,3,TimeUnit.DAYS);
            }
        });
    }


    private void saveSkuInfo( List<SeckillSessionRelationVo> data){
        data.forEach((item)->{
            List<SecKillSkuVo> relationEntities = item.getRelationEntities();
            BoundHashOperations<String, Object, Object> ops = stringRedisTemplate.boundHashOps(SKU_PREFIX);
            String randomCode = UUID.randomUUID().toString().replace("-", "");
            relationEntities.forEach((entity)->{
                //没有商品的对应key，则添加商品，并添加库存，做到幂等性
                if(!ops.hasKey(entity.getPromotionSessionId().toString()+"_"+entity.getSkuId().toString())){
                    SeckillRedisTo seckillRedisTo = new SeckillRedisTo();
                    //1. SKU的基本信息；
                    R r = productFeign.skuInfo(entity.getSkuId());
                    if(r.getCode() == 0){
                        SkuInfoVo skuInfo = r.getData("skuInfo", new TypeReference<SkuInfoVo>() {
                        });
                        seckillRedisTo.setSkuInfoVo(skuInfo);
                    }
                    //2. SKU的秒杀信息；
                    BeanUtils.copyProperties(entity,seckillRedisTo);

                    //3. 当前商品的秒杀时间信息；
                    seckillRedisTo.setStartTime(item.getStartTime().getTime());
                    seckillRedisTo.setEndTime(item.getEndTime().getTime());

                    //4. 商品的随机码 不加随机码容易遭受到攻击，不携带随机码的请求不进行处理，随机码在秒杀开始后，暴露；
                    seckillRedisTo.setRandomCode(randomCode);
                    //5. 秒杀扣除库存，不会先从数据库中扣除，在redis中设置信号量，当能从redis中改变信号量，才可以去数据库中减库存，
                    //如果redis中的信号量已经等于0，则快速结处理请求，可以应付高并发环境；
                    //设置分布式信号量； => 限流
                    RSemaphore semaphore = redissonClient.getSemaphore(SKU_SEMAPHORE + randomCode);
                    //将商品的秒杀件数作为信号量；
                    semaphore.trySetPermits(entity.getSeckillCount().intValue());
                    semaphore.expire(3,TimeUnit.DAYS);
                    //6. 将信息放入redis中
                    String string = JSON.toJSONString(seckillRedisTo);
                    ops.put(entity.getPromotionSessionId().toString()+"_"+entity.getSkuId().toString(),string);
                    stringRedisTemplate.expire(SKU_PREFIX+entity.getPromotionSessionId().toString()+"_"+entity.getSkuId().toString(),3,TimeUnit.DAYS);
                }
            });
        });
    }
}
