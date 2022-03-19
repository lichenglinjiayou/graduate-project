package com.lichenglin.gulimall.product;

import com.baomidou.mybatisplus.core.conditions.Wrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.lichenglin.gulimall.product.dao.SkuSaleAttrValueDao;
import com.lichenglin.gulimall.product.entity.BrandEntity;
import com.lichenglin.gulimall.product.service.BrandService;
import com.lichenglin.gulimall.product.service.CategoryService;
import com.lichenglin.gulimall.product.vo.spu.SkuItemSaleAttrsVo;
import lombok.extern.slf4j.Slf4j;
import org.junit.Before;
import org.junit.jupiter.api.Test;
import org.junit.runner.RunWith;
import org.redisson.api.RedissonClient;
import org.redisson.client.RedisClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import java.io.FileInputStream;
import java.io.InputStream;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;

@RunWith(SpringJUnit4ClassRunner.class)
@SpringBootTest
@Slf4j
class GulimallProductApplicationTests {

    @Autowired
    BrandService brandService;

    @Autowired
    CategoryService categoryService;

    @Autowired
    StringRedisTemplate stringRedisTemplate;

    @Autowired
    RedissonClient redissonClient;

    @Autowired
    SkuSaleAttrValueDao skuSaleAttrValueDao;


//    @Test
//    void contextLoads() {
//        QueryWrapper<BrandEntity> query = new QueryWrapper<BrandEntity>().eq("brand_id", 1);
//        BrandEntity one = brandService.getOne(query);
//        System.out.println(one);
//    }
//
//    @Test
//    void testParentPath(){
//        Long[] catelogIds = categoryService.findCatelogIds(225L);
//        log.info(Arrays.toString(catelogIds));
//    }

//    @Before
//    public void testRedis(){
//        stringRedisTemplate.opsForValue().set("hello","springboot"+ UUID.randomUUID().toString());
//    }
//    @Test
//    public void get(){
//        String hello = stringRedisTemplate.opsForValue().get("hello");
//        log.info(hello);
//    }

    @Test
    public void getRedis(){
        System.out.println(redissonClient);
    }

    @Test
    public void getSkuSale(){
        List<SkuItemSaleAttrsVo> saleAttrsBySpuId = skuSaleAttrValueDao.getSaleAttrsBySpuId((long) 7);
        System.out.println(saleAttrsBySpuId.toString());
    }
}
