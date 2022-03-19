package com.lichenglin.gulimall.order.service.impl;

import com.lichenglin.gulimall.order.entity.OrderReturnApplyEntity;
import com.rabbitmq.client.AMQP;
import com.rabbitmq.client.Channel;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.Map;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.lichenglin.common.utils.PageUtils;
import com.lichenglin.common.utils.Query;

import com.lichenglin.gulimall.order.dao.OrderItemDao;
import com.lichenglin.gulimall.order.entity.OrderItemEntity;
import com.lichenglin.gulimall.order.service.OrderItemService;


@Service("orderItemService")
public class OrderItemServiceImpl extends ServiceImpl<OrderItemDao, OrderItemEntity> implements OrderItemService {

    @Override
    public PageUtils queryPage(Map<String, Object> params) {
        IPage<OrderItemEntity> page = this.page(
                new Query<OrderItemEntity>().getPage(params),
                new QueryWrapper<OrderItemEntity>()
        );

        return new PageUtils(page);
    }

    /*
        1.message:原生消息详细信息：消息头+消息体
        2.T<发送消息的类型>
        3.Channel channel => 当前传输数据的通道
        queue可以很多人同时监听，只要收到消息，则会删除，只能有一个收到此消息；
        一个消息处理完，才能出来下一个消息；
        @RabbitListener = >  类+方法上；（监听哪些队列）
        @RabbitHandler = > 方法；（重载不同的接收消息的方法）
     */
    @RabbitListener(queues = {"java_queue"})
    public void messageListener(Message message, OrderReturnApplyEntity entity, Channel channel){
//        System.out.println("消息内容："+message+",消息类型："+entity);
        //在channel内按照顺序自增
        long deliveryTag = message.getMessageProperties().getDeliveryTag();
        //false - 非批量签收，只确认签收当前的消息
        // 如果出现异常，是因为网络通道出现问题
        try {
            if(deliveryTag % 2 == 0){
                channel.basicAck(deliveryTag,false);
                System.out.println("签收了货物："+deliveryTag);
            }else{
                //参数3：拒收的消息是否重新入队；
                channel.basicNack(deliveryTag,false,true);
                System.out.println("拒收了货物："+deliveryTag);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

}