package com.lichenglin.gulimall.repository.config;

import org.springframework.amqp.core.*;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.amqp.rabbit.connection.CorrelationData;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.amqp.support.converter.MessageConverter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import javax.annotation.PostConstruct;
import java.util.HashMap;
import java.util.Map;

@Configuration
public class MyRabbitConfig {


    /*
        向容器中放入jackson2Josn的消息转化器；此后的所有对象都会转化为json对象发送；
     */
    @Bean
    public MessageConverter messageConverter(){
        return new Jackson2JsonMessageConverter();
    }



    @Bean
    public Exchange stockEventExchange(){
        return new TopicExchange("stock_event_exchange",true,false);
    }

    @Bean
    public Queue stockDelayQueue(){
        Map<String, Object> arguments = new HashMap<>();
        arguments.put("x-dead-letter-exchange","stock-event-exchange");
        arguments.put("x-dead-letter-routing-key","stock.released");
        arguments.put("x-message-ttl",60000 * 2);
        return new Queue("stock.delay.queue",true,false,false,arguments);
    }

    @Bean
    public Queue stockReleaseQueue(){
        return new Queue("stock.release.queue",true,false,false);
    }

    @Bean
    public Binding delayBinding(){
        return new Binding("stock.delay.queue", Binding.DestinationType.QUEUE,"stock-event-exchange","stock.locked",null);
    }

    @Bean
    public Binding releaseBinding(){
        return new Binding("stock.release.queue", Binding.DestinationType.QUEUE,"stock-event-exchange","stock.released.#",null);
    }
}
