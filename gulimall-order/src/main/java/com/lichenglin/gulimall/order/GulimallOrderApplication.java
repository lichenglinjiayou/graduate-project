package com.lichenglin.gulimall.order;

import com.alibaba.cloud.seata.GlobalTransactionAutoConfiguration;
import io.seata.spring.annotation.GlobalTransactional;
import org.springframework.amqp.rabbit.annotation.EnableRabbit;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.context.annotation.EnableAspectJAutoProxy;
import org.springframework.session.data.redis.config.annotation.web.http.EnableRedisHttpSession;

/**
 * 1.引入amqp场景；给容器中自动配置RabbitTemplate、AmqpAdmin、CachingConnectionFactory、RabbitMessagingTemplate
 * 2.监听消息，则必须标注@EnableRabbit注解
 */
@EnableRabbit
@SpringBootApplication(exclude = GlobalTransactionAutoConfiguration.class)
@EnableDiscoveryClient
@EnableRedisHttpSession
@EnableFeignClients
// 以后动态代理均由aspectJ创建；没有接口也可以创建；
// 对外暴露代理对象；
/**
 * seata 控制分布式事务；
 * 1. 每一个微服务创建undo log 表；
 * 2. 安装tc:事务协调器；
 * 3. 3.1 导入起步依赖
 *    3.2 改变registry.conf
 *    3.3 所有需要分布式事务的微服务，应该使用seata代理数据源；
 *    3.4 每个微服务都必须导入file.conf \ registry.conf
 *    3.5  vgroup_mapping.gulimall-order-fescar-service-group = "default"
 *    3.6 非分布式事务入口标注全局事务；
 */
@EnableAspectJAutoProxy(exposeProxy = true)
public class GulimallOrderApplication {

    public static void main(String[] args) {
        SpringApplication.run(GulimallOrderApplication.class, args);
    }

}
