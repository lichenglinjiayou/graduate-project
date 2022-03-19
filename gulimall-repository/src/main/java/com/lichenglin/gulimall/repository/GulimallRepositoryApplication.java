package com.lichenglin.gulimall.repository;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.amqp.rabbit.annotation.EnableRabbit;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.transaction.annotation.EnableTransactionManagement;

@EnableTransactionManagement
@MapperScan(basePackages = {"com.lichenglin.gulimall.repository.dao"})
@EnableDiscoveryClient
@SpringBootApplication
@EnableFeignClients
@EnableRabbit
public class GulimallRepositoryApplication {

    public static void main(String[] args) {
        SpringApplication.run(GulimallRepositoryApplication.class, args);
    }

}
/**
 * 2022/2/27
 * 引入模板引擎，关闭thymeleaf缓存；
 * 静态资源放在static文件夹下，就可以按照路径直接访问
 * 页面放在templates下，可以直接访问，springboot访问项目时，默认会找index；
 */
