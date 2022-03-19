package com.lichenglin.gulimall.repository.listener;

import com.alibaba.fastjson.TypeReference;
import com.lichenglin.common.enums.OrderStatusEnum;
import com.lichenglin.common.to.StockDetailTo;
import com.lichenglin.common.to.StockLockedTo;
import com.lichenglin.common.to.mq.OrderTo;
import com.lichenglin.common.utils.R;
import com.lichenglin.gulimall.repository.entity.WmsWareOrderTaskDetailEntity;
import com.lichenglin.gulimall.repository.entity.WmsWareOrderTaskEntity;
import com.lichenglin.gulimall.repository.feign.OrderFeign;
import com.lichenglin.gulimall.repository.service.WmsWareOrderTaskDetailService;
import com.lichenglin.gulimall.repository.service.WmsWareOrderTaskService;
import com.lichenglin.gulimall.repository.service.WmsWareSkuService;
import com.lichenglin.gulimall.repository.vo.OrderVo;
import com.rabbitmq.client.Channel;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.rabbit.annotation.RabbitHandler;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.IOException;
@Service
@RabbitListener(queues = "stock.release.queue")
public class StockReleaseListener {
    @Autowired
    WmsWareSkuService wmsWareSkuService;
    @RabbitHandler
    public void handleStockLockedRelease(StockLockedTo stockLockedTo, Message message, Channel channel) throws IOException {
        System.out.println("收到解锁库存消息");
        try {
            wmsWareSkuService.unlockStock(stockLockedTo);
            channel.basicAck(message.getMessageProperties().getDeliveryTag(),false);
        } catch (Exception e) {
            channel.basicReject(message.getMessageProperties().getDeliveryTag(),true);
        }

    }

    public void orderCloseRelease(OrderTo orderTo,Message message,Channel channel) throws IOException {
        System.out.println("订单关闭，准备解锁库存");
        try {
            wmsWareSkuService.unlockStockOrderTo(orderTo);
            channel.basicAck(message.getMessageProperties().getDeliveryTag(),false);
        } catch (Exception e) {
            channel.basicReject(message.getMessageProperties().getDeliveryTag(),true);
        }
    }
}
