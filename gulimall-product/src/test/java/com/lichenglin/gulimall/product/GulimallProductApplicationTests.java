package com.lichenglin.gulimall.product;

import com.baomidou.mybatisplus.core.conditions.Wrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.lichenglin.gulimall.product.entity.BrandEntity;
import com.lichenglin.gulimall.product.service.BrandService;
import com.lichenglin.gulimall.product.service.CategoryService;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import java.io.FileInputStream;
import java.io.InputStream;
import java.util.Arrays;

@RunWith(SpringJUnit4ClassRunner.class)
@SpringBootTest
@Slf4j
class GulimallProductApplicationTests {

    @Autowired
    BrandService brandService;

    @Autowired
    CategoryService categoryService;


    @Test
    void contextLoads() {
        QueryWrapper<BrandEntity> query = new QueryWrapper<BrandEntity>().eq("brand_id", 1);
        BrandEntity one = brandService.getOne(query);
        System.out.println(one);
    }

    @Test
    void testParentPath(){
        Long[] catelogIds = categoryService.findCatelogIds(225L);
        log.info(Arrays.toString(catelogIds));
    }



}
