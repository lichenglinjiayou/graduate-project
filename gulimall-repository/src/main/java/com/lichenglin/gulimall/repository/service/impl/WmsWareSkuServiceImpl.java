package com.lichenglin.gulimall.repository.service.impl;

import com.alibaba.fastjson.TypeReference;
import com.lichenglin.common.enums.OrderStatusEnum;
import com.lichenglin.common.to.StockDetailTo;
import com.lichenglin.common.to.StockLockedTo;
import com.lichenglin.common.to.es.SkuEsModel;
import com.lichenglin.common.to.mq.OrderTo;
import com.lichenglin.common.utils.R;
import com.lichenglin.gulimall.repository.entity.WmsWareOrderTaskDetailEntity;
import com.lichenglin.gulimall.repository.entity.WmsWareOrderTaskEntity;
import com.lichenglin.gulimall.repository.exception.NoStockException;
import com.lichenglin.gulimall.repository.feign.OrderFeign;
import com.lichenglin.gulimall.repository.service.WmsWareOrderTaskDetailService;
import com.lichenglin.gulimall.repository.service.WmsWareOrderTaskService;
import com.lichenglin.gulimall.repository.vo.*;
import com.rabbitmq.client.Channel;
import lombok.Data;
import org.apache.commons.lang.StringUtils;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.rabbit.annotation.RabbitHandler;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.lichenglin.common.utils.PageUtils;
import com.lichenglin.common.utils.Query;

import com.lichenglin.gulimall.repository.dao.WmsWareSkuDao;
import com.lichenglin.gulimall.repository.entity.WmsWareSkuEntity;
import com.lichenglin.gulimall.repository.service.WmsWareSkuService;
import org.springframework.transaction.annotation.Transactional;


@Service("wmsWareSkuService")
public class WmsWareSkuServiceImpl extends ServiceImpl<WmsWareSkuDao, WmsWareSkuEntity> implements WmsWareSkuService {


    @Autowired
    RabbitTemplate rabbitTemplate;
    @Autowired
    WmsWareOrderTaskService wareOrderTaskService;
    @Autowired
    WmsWareOrderTaskDetailService wmsWareOrderTaskDetailService;
    @Autowired
    OrderFeign orderFeign;

    @Override
    public PageUtils queryPage(Map<String, Object> params) {
        IPage<WmsWareSkuEntity> page = this.page(
                new Query<WmsWareSkuEntity>().getPage(params),
                new QueryWrapper<WmsWareSkuEntity>()
        );

        return new PageUtils(page);
    }

    @Override
    public PageUtils queryPageByCondition(Map<String, Object> params) {
        QueryWrapper<WmsWareSkuEntity> wrapper = new QueryWrapper<>();
        String skuId = (String) params.get("skuId");
        String wareId = (String) params.get("wareId");

        if (!StringUtils.isEmpty(skuId)) {
            wrapper.eq("sku_id", skuId);
        }
        if (!StringUtils.isEmpty(wareId)) {
            wrapper.eq("ware_id", wareId);
        }

        IPage<WmsWareSkuEntity> page = this.page(
                new Query<WmsWareSkuEntity>().getPage(params),
                wrapper
        );

        return new PageUtils(page);
    }

    @Override
    public void saveSkuInfo(WmsWareSkuEntity wmsWareSkuEntity) {

        this.baseMapper.saveSkuInfo(wmsWareSkuEntity);
    }

    @Override
    public List<SkuHasStockVo> hasStock(List<Long> skuIds) {

        List<SkuHasStockVo> list = new ArrayList<>();
        skuIds.forEach((item) -> {
            SkuHasStockVo skuHasStockVo = new SkuHasStockVo();
            StackInfoVo stackInfoVo = this.baseMapper.getStock(item);
            if (stackInfoVo != null) {
                skuHasStockVo.setSkuId(item);
                skuHasStockVo.setHasStock(stackInfoVo.getStocks() - (stackInfoVo.getStockLocks() == null ? 0 : stackInfoVo.getStockLocks()) > 0 ? true : false);
                list.add(skuHasStockVo);
            }
        });
        return list;
    }

    /*
        库存自动解锁
     */


    private void unlockStock(Long skuId, Long wareId, Integer num, Long taskDetailId) {
        this.baseMapper.unlockStock(skuId, wareId, num);
        //更新库存工作单的状态
        WmsWareOrderTaskDetailEntity detailEntity = new WmsWareOrderTaskDetailEntity();
        detailEntity.setLockStatus(2);
        detailEntity.setId(taskDetailId);
        wmsWareOrderTaskDetailService.updateById(detailEntity);
    }

    /**
     * 为订单锁定库存
     * 1, 下订单成功，订单过期没有支付被系统自动取消，或者被用户手动取消，都需要解锁库存；
     * 2. 下订单成功，库存成功锁定，其他业务调用失败，导致订单回滚，之前锁定的库存需要自动解锁；
     *
     * @param wareSkuLockVo
     * @return
     */
    @Transactional(rollbackFor = NoStockException.class)
    @Override
    public Boolean lockOrder(WareSkuLockVo wareSkuLockVo) {

        /*
            保存库存工作单的信息
         */
        WmsWareOrderTaskEntity taskEntity = new WmsWareOrderTaskEntity();
        taskEntity.setOrderSn(wareSkuLockVo.getOrderSn());
        wareOrderTaskService.save(taskEntity);

        //1.找到每个商品的存放仓库；
        List<OrderItemVo> locks = wareSkuLockVo.getLocks();
        List<SkuWareHasSock> list = new ArrayList<>();
        locks.forEach((item) -> {
            SkuWareHasSock skuWareHasSock = new SkuWareHasSock();
            Long skuId = item.getSkuId();
            skuWareHasSock.setSkuId(skuId);
            List<Long> wareIds = new ArrayList<>();
            List<WmsWareSkuEntity> wareSkuEntities = this.list(new QueryWrapper<WmsWareSkuEntity>().eq("sku_id", skuId));
            skuWareHasSock.setNum(item.getCount());
            for (WmsWareSkuEntity wareSkuEntity : wareSkuEntities) {
                if (wareSkuEntity.getStock() - wareSkuEntity.getStockLocked() > 0) {
                    wareIds.add(wareSkuEntity.getWareId());
                }
            }
            skuWareHasSock.setWareId(wareIds);
            list.add(skuWareHasSock);
        });
        Boolean allLock = true;
        for (SkuWareHasSock skuWareHasSock : list) {
            Boolean skuStocked = false;
            Long skuId = skuWareHasSock.getSkuId();
            List<Long> wareIds = skuWareHasSock.getWareId();
            if (wareIds == null || wareIds.size() == 0) {
                throw new NoStockException(skuId);
            }
            //锁定失败，也可以发送消息到队列，一旦回滚，数据库无对应Id，也不存在解锁的问题；
            for (Long wareId : wareIds) {
                //锁定是否成功根据count值的大小来决定；1-锁定成功；0-锁定失败
                Long count = this.baseMapper.lockSkuStock(skuId, wareId, skuWareHasSock.getNum());
                if (count == 0) {
                    //当前仓库锁定失败，尝试下一个仓库；

                } else {
                    skuStocked = true;
                    // 锁定成功，发送消息，告诉MQ库存锁定成功
                    WmsWareOrderTaskDetailEntity detailEntity = new WmsWareOrderTaskDetailEntity(null, skuId, null, skuWareHasSock.getNum(), taskEntity.getId(), wareId, 1);
                    wmsWareOrderTaskDetailService.save(detailEntity);
                    StockLockedTo stockLockedTo = new StockLockedTo();
                    stockLockedTo.setId(taskEntity.getId());
                    StockDetailTo stockDetailTo = new StockDetailTo();
                    BeanUtils.copyProperties(detailEntity, stockDetailTo);
                    stockLockedTo.setDetail(stockDetailTo);
                    // TODO: mq队列中没有对应的消息；
                    rabbitTemplate.convertAndSend("stock_event_exchange", "stock.locked", stockLockedTo);
                    break;
                }
            }
            //存在商品没有锁住
            if (skuStocked == false) {
                throw new NoStockException(skuId);
            }
        }


        //所有商品锁定成功
        return true;
    }

    @Override
    public void unlockStock(StockLockedTo stockLockedTo) {
        StockDetailTo detail = stockLockedTo.getDetail();
        Long id = detail.getId();
        //查询数据库关于订单的锁库存信息，如果数据库没有，库存锁定失败，已经发生回滚，无需解锁；
        //有的话，需要解锁；
        WmsWareOrderTaskDetailEntity byId = wmsWareOrderTaskDetailService.getById(id);
        if (byId != null) {
            //解锁, 表示库存锁定成功，至于是否解锁，还要参考订单的情况，如果没有订单，则说明有操作发生
            //回滚，导致订单没有生成，因此需要解锁； 有订单，则查看订单状态，如果是取消，则解锁；没有取消，
            //则不能解锁库存；
            Long orderId = stockLockedTo.getId();
            //查询订单信息
            WmsWareOrderTaskEntity orderTaskEntity = wareOrderTaskService.getById(orderId);
            //查询订单状态
            String orderSn = orderTaskEntity.getOrderSn();
            R r = orderFeign.getOrderStatus(orderSn);
            if (r.getCode() == 0) {
                OrderVo data = r.getData(new TypeReference<OrderVo>() {
                });
                //获取到订单状态
                Integer status = data.getStatus();
                if (data == null || status == OrderStatusEnum.CANCLED.getCode()) {
                    //只有工作单状态为1，即为锁定状态时，才可以解锁；
                    if(byId.getLockStatus() == 1){
                        unlockStock(detail.getSkuId(), detail.getWareId(), detail.getSkuNum(), detail.getId());
                    }
                }
            }else{
                //拒绝消息后，消息重新入队，让其他再继续解锁
                throw new RuntimeException("远程服务失败");
            }
        }else{
            //无需解锁
        }
    }

    /**
     * 防止订单服务卡顿，导致订单状态一致未发生改变，库存消息优先到期，查看订单状态
     * 为新创建状态，无法对订单进行有效处理，因此需要在订单到期后，可以自行调用库存的解锁操作；
     * @param orderTo
     */
    @Override
    @Transactional
    public void unlockStockOrderTo(OrderTo orderTo) {
        String s = orderTo.getOrderSn();
        //查询最新的订单状态
        R r = orderFeign.getOrderStatus(s);
        //查询最新的库存解锁状态，防止已经解锁后，再次进行重复解锁；
        WmsWareOrderTaskEntity orderTaskEntity = wareOrderTaskService.getOne(new QueryWrapper<WmsWareOrderTaskEntity>().eq("order_sn", s));
        //按照库存工作单找到没有解锁的库存
        List<WmsWareOrderTaskDetailEntity> entities = wmsWareOrderTaskDetailService.list(new QueryWrapper<WmsWareOrderTaskDetailEntity>().eq("task_id", orderTaskEntity.getId()).eq("lock_status", 1));

        entities.forEach((item)->{
            unlockStock(item.getSkuId(),item.getWareId(),item.getSkuNum(),item.getTaskId());
        });
    }

    @Data
    class SkuWareHasSock {
        private Long skuId;
        private List<Long> wareId;
        private Integer num;
    }
}