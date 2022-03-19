package com.lichenglin.seckill.to;

import com.baomidou.mybatisplus.annotation.TableId;
import com.lichenglin.seckill.vo.SkuInfoVo;
import lombok.Data;

import java.math.BigDecimal;

@Data
public class SeckillRedisTo {
    /**
     * id
     */
    @TableId
    private Long id;
    /**
     * 活动id
     */
    private Long promotionId;
    /**
     * 活动场次id
     */
    private Long promotionSessionId;
    /**
     * 商品id
     */
    private Long skuId;
    /**
     * 秒杀价格
     */
    private BigDecimal seckillPrice;
    /**
     * 秒杀总量
     */
    private BigDecimal seckillCount;
    /**
     * 每人限购数量
     */
    private BigDecimal seckillLimit;
    /**
     * 排序
     */
    private Integer seckillSort;

    // redis中保存商品的详细信息，高并发环境下，无需查询数据库，直接从redis中获取；
    private SkuInfoVo skuInfoVo;


    /**
     * 当前商品秒杀的开始和结束时间
     */
    private  Long startTime;
    private Long endTime;

    // 商品秒杀随机码
    private String randomCode;
}
