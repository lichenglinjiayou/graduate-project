package com.lichenglin.seckill;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.session.data.redis.config.annotation.web.http.EnableRedisHttpSession;

/**
 * SpringBoot 整合sentinel:
 * 1. 导入sentinel起步依赖；
 * 2. 下载sentinel-dashboard控制台；
 * 3. 配置sentinel地址信息；
 * 4. 在控制台设置流控规则，但是流控规则保存在内存中，重启失效；
 * 5. 每个微服务都要导入actuator信息审计依赖；
 *      暴露端口;
 * 6. 自定义sentinel流控返回数据；
 * 7.自定义受保护的资源；
 *      try(Entry entry = SphU.entry("xxx")){受保护的资源}catch(BlockException e){}
 *      注解：@SentinelResource - 一定要配置被限流以后的默认返回；
 *      url请求可以设置统一返回；
 */
@EnableDiscoveryClient
@EnableFeignClients
@EnableRedisHttpSession
@SpringBootApplication(exclude = DataSourceAutoConfiguration.class)
public class GulimallSeckillApplication {

    public static void main(String[] args) {
        SpringApplication.run(GulimallSeckillApplication.class, args);
    }

}
