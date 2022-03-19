package com.lichenglin.gulimall.order;

import com.lichenglin.gulimall.order.entity.OrderReturnApplyEntity;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;
import org.springframework.amqp.core.*;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

@Slf4j
@SpringBootTest
class GulimallOrderApplicationTests {


    /**
     *  1.使用Java代码创建交换机、队列、绑定二者之间的关系
     *  2.收发消息
     */

    @Autowired
    AmqpAdmin amqpAdmin;

    @Autowired
    RabbitTemplate rabbitTemplate;

    @Test
    public void testSendMessage(){
        /*
            1. 如何发送的消息是对象，则会序列化对象，因此对象必须实现serializable接口；
            2. 发送的对象可以是JSON字符串；
         */
        for(int i = 0;i<5;i++){
            OrderReturnApplyEntity orderReturnApplyEntity = new OrderReturnApplyEntity();
            orderReturnApplyEntity.setId(1L);
            orderReturnApplyEntity.setCompanyAddress("西安");
            rabbitTemplate.convertAndSend("java_exchange","java_queue",orderReturnApplyEntity);
            log.info("消息发送完成{}",orderReturnApplyEntity);
        }
    }

    @Test
    void createExchange() {
        //1. 创建交换机
        // String name, boolean durable, boolean autoDelete, Map<String, Object> arguments
        Exchange exchange = new DirectExchange("java_exchange",true,false);
        amqpAdmin.declareExchange(exchange);
        log.info("创建成功：" + exchange.getName());
    }
    @Test
    void createQueue(){
        Queue queue = new Queue("java_queue",true,false,false);
        amqpAdmin.declareQueue(queue);
        log.info("创建成功：" + queue.getName());
    }

    @Test
    void createBinding(){
//    String destination, Binding.DestinationType destinationType, String exchange, String routingKey, Map<String, Object> arguments
        Binding binding = new Binding("java_queue", Binding.DestinationType.QUEUE,"java_exchange","java_queue",null);
        amqpAdmin.declareBinding(binding);
        log.info("创建成功：binding" );
    }
}
