package com.lichenglin.gulimall.user;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.cloud.openfeign.EnableFeignClients;

@SpringBootApplication
@EnableDiscoveryClient
@EnableFeignClients(basePackages = "com.lichenglin.gulimall.user.feign")
public class GulimallUserApplication {

    public static void main(String[] args) {
        SpringApplication.run(GulimallUserApplication.class, args);
    }

}
