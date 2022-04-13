package com.lichenglin.gulimall.product.config;

import org.redisson.Redisson;
import org.redisson.api.RedissonClient;
import org.redisson.config.Config;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class MyRedissonConfig {
    //1. add redissonClient into the container;
    @Bean(destroyMethod = "shutdown")
    public RedissonClient redissonClient() {
        //2. create Configuration class;
        Config config = new Config();
        //3. set single redis server mode and the host address and port of redis server;
        config.useSingleServer().setAddress("redis://192.168.127.138:6379");
        //4. create RedissonClient using customized configuration;
        return Redisson.create(config);
    }
}
