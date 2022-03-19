package com.lichenglin.gulimall.order.config;

import com.lichenglin.gulimall.order.entity.OrderEntity;
import com.rabbitmq.client.AMQP;
import com.rabbitmq.client.Channel;
import org.springframework.amqp.core.*;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

@Configuration
public class MyMqConfig {

    //死信队列
    @Bean
    public Queue orderDelayQueue(){
        Map<String, Object> arguments = new HashMap<>();
        arguments.put("x-dead-letter-exchange","order-event-exchange");
        arguments.put("x-dead-letter-routing-key","order.release.order");
        arguments.put("x-message-ttl",60000);
       return new Queue("order.delay.queue",true,false,false,arguments);

    }


    @Bean
    public Queue orderReleaseQueue(){
        return new Queue("order.release.queue",true,false,false);
    }

    @Bean
    public Exchange orderEventExchange(){
        return new TopicExchange("order-event-exchange",true,false);
    }

    @Bean
    public Binding orderCreateOrderBinding(){
        return new Binding("order.delay.queue", Binding.DestinationType.QUEUE,"order-event-exchange","order.create.order",null);
    }

    @Bean
    public Binding orderReleaseOrderBinding(){
        return new Binding("order.release.queue", Binding.DestinationType.QUEUE,"order-event-exchange","order.release.order",null);
    }

    @Bean
    public Binding stockReleasedOtherBinding(){
        return new Binding("stock.release.queue", Binding.DestinationType.QUEUE,"order-event-exchange","order.released.#",null);
    }

    @Bean
    public Queue orderSecKillQueue(){
        return new Queue("order.seckill.queue",true,false,false,null);
    }

    @Bean
    public Binding orderSecKillReleaseBinding(){
        return new Binding("order.seckill.queue", Binding.DestinationType.QUEUE,"order-event-exchange","order.seckill.order",null);
    }

}
