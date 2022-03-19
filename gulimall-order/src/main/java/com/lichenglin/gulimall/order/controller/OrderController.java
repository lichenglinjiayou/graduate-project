package com.lichenglin.gulimall.order.controller;

import java.util.Arrays;
import java.util.Map;

import com.lichenglin.gulimall.order.entity.OrderReturnApplyEntity;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import com.lichenglin.gulimall.order.entity.OrderEntity;
import com.lichenglin.gulimall.order.service.OrderService;
import com.lichenglin.common.utils.PageUtils;
import com.lichenglin.common.utils.R;



/**
 * 订单
 *
 * @author lichenglin
 * @email 13384966629@163.com
 * @date 2022-02-09 21:31:04
 */
@RestController
@RequestMapping("order/order")
public class OrderController {
    @Autowired
    private OrderService orderService;


    @Autowired
    private RabbitTemplate rabbitTemplate;

    /**
     * 列表
     */
    @RequestMapping("/list")
    public R list(@RequestParam Map<String, Object> params){
        PageUtils page = orderService.queryPage(params);

        return R.ok().put("page", page);
    }


    /**
     * 信息
     */
    @RequestMapping("/info/{id}")
    public R info(@PathVariable("id") Long id){
		OrderEntity order = orderService.getById(id);

        return R.ok().put("order", order);
    }

    /**
     * 保存
     */
    @RequestMapping("/save")
    public R save(@RequestBody OrderEntity order){
		orderService.save(order);

        return R.ok();
    }

    /**
     * 修改
     */
    @RequestMapping("/update")
    public R update(@RequestBody OrderEntity order){
		orderService.updateById(order);

        return R.ok();
    }

    /**
     * 删除
     */
    @RequestMapping("/delete")
    public R delete(@RequestBody Long[] ids){
		orderService.removeByIds(Arrays.asList(ids));

        return R.ok();
    }

    @GetMapping("/sendMsg")
    @ResponseBody
    public String sendMsg(){
        for(int i = 0;i<5;i++){
            OrderReturnApplyEntity orderReturnApplyEntity = new OrderReturnApplyEntity();
            orderReturnApplyEntity.setId(1L);
            orderReturnApplyEntity.setCompanyAddress("西安");
            rabbitTemplate.convertAndSend("java_exchange","java_queue",orderReturnApplyEntity);
        }
        return "ok";
    }

    @GetMapping("/status/{orderSn}")
    public R getOrderStatus(@PathVariable("orderSn") String orderSn){
       OrderEntity orderEntity =  orderService.getOrderByOrderSn(orderSn);
       return  R.ok().setData(orderEntity);
    }

    @PostMapping("/listWithItem")
    public R getItemList(@RequestBody Map<String,Object> params){
        PageUtils page = orderService.queryPageWithItem(params);
        return R.ok().setData(page);
    }
}
