package com.lichenglin.gulimall.order.listener;

import com.lichenglin.gulimall.order.entity.OrderEntity;
import com.lichenglin.gulimall.order.service.OrderService;
import com.rabbitmq.client.Channel;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.rabbit.annotation.RabbitHandler;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.IOException;

@Service
@RabbitListener(queues = "order.release.queue")
public class OrderCloseListener {

    @Autowired
    OrderService orderService;

    @RabbitHandler
    public void Listener(OrderEntity orderEntity, Channel channel, Message message) throws IOException {
        System.out.println("收到过期的订单信息，准备关闭订单："+orderEntity);
        try {
            orderService.closeOrder(orderEntity);
            //TODO:手动调用支付宝收单
            channel.basicAck(message.getMessageProperties().getDeliveryTag(),false);
        } catch (IOException e) {
            //拒绝后，消息重新放回消息队列；
            channel.basicReject(message.getMessageProperties().getDeliveryTag(),true);
        }
    }
}
