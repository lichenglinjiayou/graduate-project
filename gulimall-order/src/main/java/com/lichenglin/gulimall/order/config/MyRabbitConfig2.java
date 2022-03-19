package com.lichenglin.gulimall.order.config;

import org.springframework.amqp.core.Message;
import org.springframework.amqp.rabbit.connection.CorrelationData;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;

import javax.annotation.PostConstruct;

@Configuration
public class MyRabbitConfig2 {

    @Autowired
    RabbitTemplate template;


    @PostConstruct
    public void initRabbitTemplate(){
        template.setConfirmCallback(new RabbitTemplate.ConfirmCallback() {
            @Override
            // 1：当前消息的唯一关联数据；
            //2： 消息是否成功收到
            //3： 失败原因
            public void confirm(CorrelationData correlationData, boolean b, String s) {
                System.out.println("消息抵达交换机=>"+correlationData+"=>"+b+"=>"+s);
            }
        });

        /**
         * message : 投递到队列失败的消息；
         * i: 回复的状态码
         * s: 回复的文本内容
         * s1: 消息发给哪个交换机
         * s2： 消息使用哪个routing key;
         */
        template.setReturnCallback(new RabbitTemplate.ReturnCallback() {
            @Override
            public void returnedMessage(Message message, int i, String s, String s1, String s2) {
                System.out.println("消息抵达队列=>"+message+"=>"+i+"=>"+s+"=>"+s1+"=>"+s2);
            }
        });
    }

    /**
     * 消费端确认；自动确认，只要消息接收到，客户端则会自动确认，服务器端会移除确认的消息；
     * 问题：消息回复成功，但是消息在处理过程中发生宕机故障，造成消息丢失；
     * 手动确认，处理一个，确认一个;只要没有明确告诉MQ，获取被签收，则消息一直是unacked状态；
     * 如何签收？
     *  basicAck(); - 签收消息
     *  basicNack(); - 拒收消息
     */
}
