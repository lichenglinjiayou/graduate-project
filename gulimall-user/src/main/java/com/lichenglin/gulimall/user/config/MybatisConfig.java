package com.lichenglin.gulimall.user.config;

import com.baomidou.mybatisplus.extension.plugins.PaginationInterceptor;
import org.mybatis.spring.annotation.MapperScan;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.transaction.annotation.EnableTransactionManagement;

@EnableTransactionManagement
@Configuration
@MapperScan(basePackages = {"com.lichenglin.gulimall.user.dao"})
/*
    引入分页插件
 */
public class MybatisConfig {

    @Bean
    public PaginationInterceptor paginationInterceptor(){
        PaginationInterceptor paginationInterceptor= new PaginationInterceptor();
        //设置当请求的页码大于最后一页时，返回第一页的数据；如果为false,则继续请求空数据，默认为false;
        paginationInterceptor.setOverflow(true);
        //设置最大单页限制数量，默认为500，-1则不受限制；
        paginationInterceptor.setLimit(1000);
        return paginationInterceptor;
    }
}
