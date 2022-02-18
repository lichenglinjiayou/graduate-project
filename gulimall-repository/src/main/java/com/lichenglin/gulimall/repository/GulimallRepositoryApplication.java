package com.lichenglin.gulimall.repository;

import org.mybatis.spring.annotation.MapperScan;
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
public class GulimallRepositoryApplication {

    public static void main(String[] args) {
        SpringApplication.run(GulimallRepositoryApplication.class, args);
    }

}
