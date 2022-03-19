package com.lichenglin.gulimall.order.service.impl;

import com.alibaba.fastjson.TypeReference;
import com.baomidou.mybatisplus.core.toolkit.IdWorker;
import com.lichenglin.common.constant.OrderConstant;
import com.lichenglin.common.exception.NoStockException;
import com.lichenglin.common.to.SeckillOrderTo;
import com.lichenglin.common.to.mq.OrderTo;
import com.lichenglin.common.utils.R;
import com.lichenglin.common.vo.UserLoginVo;
import com.lichenglin.gulimall.order.entity.OrderItemEntity;
import com.lichenglin.gulimall.order.entity.PaymentInfoEntity;
import com.lichenglin.gulimall.order.enume.OrderStatusEnum;
import com.lichenglin.gulimall.order.feign.CartFeign;
import com.lichenglin.gulimall.order.feign.ProductFeign;
import com.lichenglin.gulimall.order.feign.RepositoryFeign;
import com.lichenglin.gulimall.order.feign.UserFeign;
import com.lichenglin.gulimall.order.interceptor.LoginInterceptor;
import com.lichenglin.gulimall.order.service.OrderItemService;
import com.lichenglin.gulimall.order.service.PaymentInfoService;
import com.lichenglin.gulimall.order.to.OrderCreateTo;
import com.lichenglin.gulimall.order.vo.*;
import io.seata.spring.annotation.GlobalTransactional;
import org.springframework.amqp.AmqpException;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.script.DefaultRedisScript;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.lichenglin.common.utils.PageUtils;
import com.lichenglin.common.utils.Query;

import com.lichenglin.gulimall.order.dao.OrderDao;
import com.lichenglin.gulimall.order.entity.OrderEntity;
import com.lichenglin.gulimall.order.service.OrderService;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;
import org.springframework.web.context.request.RequestAttributes;
import org.springframework.web.context.request.RequestContextHolder;


@Service("orderService")
public class OrderServiceImpl extends ServiceImpl<OrderDao, OrderEntity> implements OrderService {

    @Autowired
    UserFeign userFeign;
    @Autowired
    CartFeign cartFeign;
    @Autowired
    ThreadPoolExecutor threadPoolExecutor;
    @Autowired
    RepositoryFeign repositoryFeign;
    @Autowired
    StringRedisTemplate redisTemplate;
    @Autowired
    ProductFeign productFeign;
    @Autowired
    OrderItemService orderItemService;
    @Autowired
    RabbitTemplate rabbitTemplate;
    @Autowired
    PaymentInfoService paymentInfoService;

    private ThreadLocal<OrderSubmitVo> threadLocal = new ThreadLocal<>();

    @Override
    public PageUtils queryPage(Map<String, Object> params) {
        IPage<OrderEntity> page = this.page(
                new Query<OrderEntity>().getPage(params),
                new QueryWrapper<OrderEntity>()
        );

        return new PageUtils(page);
    }

    @Override
    public OrderConfirmVo confirmOrder() {
        OrderConfirmVo confirmVo = new OrderConfirmVo();
        //从主线程获取原来的数据
        RequestAttributes requestAttributes = RequestContextHolder.getRequestAttributes();
        UserLoginVo userLoginVo = LoginInterceptor.threadLocal.get();
        CompletableFuture<Void> getAddress = CompletableFuture.runAsync(() -> {
            //在异步线程对获取的原来的请求数据进行共享
            RequestContextHolder.setRequestAttributes(requestAttributes);
            // 查询地址
            List<MemberAddressVo> address = userFeign.getAddress(Long.parseLong(userLoginVo.getId()));
            confirmVo.setAddress(address);
        }, threadPoolExecutor);

        CompletableFuture<Void> getCartItems = CompletableFuture.runAsync(() -> {
            // 查询购物车中的所有购物项
        /*
        feign在远程调用之前，会构造请求，调用拦截器增加属性；
        如果没有拦截器，则直接发送原生请求；（feign远程调用会丢失请求头）
         */
            //在异步线程对获取的原来的请求数据进行共享
            RequestContextHolder.setRequestAttributes(requestAttributes);
            List<OrderItemVo> cartItems = cartFeign.getCartItems();

            System.out.println(cartItems);
            confirmVo.setItems(cartItems);
        }, threadPoolExecutor).thenRunAsync(()->{
            List<OrderItemVo> items = confirmVo.getItems();
            List<Long> skuIds = new ArrayList<>();
            items.forEach((item->{
                Long skuId = item.getSkuId();
                skuIds.add(skuId);
            }));

            R r = repositoryFeign.getSkusHasStock(skuIds);
            List<SkuHasStockVo> data = r.getData(new TypeReference<List<SkuHasStockVo>>() {
            });
            if(data != null){
                Map<Long, Boolean> map = data.stream().collect(Collectors.toMap(SkuHasStockVo::getSkuId, SkuHasStockVo::getHasStock));
                confirmVo.setStock(map);
            }
        },threadPoolExecutor);

        CompletableFuture<Void> all = CompletableFuture.allOf(getAddress, getCartItems);
        all.join();
        //TODO: 查询用户积分
        //其余数据自动获取
        //TODO: 防重令牌
        String token = UUID.randomUUID().toString().replace("-","");
        //将防重令牌存到浏览器
        confirmVo.setOrderToken(token);
        //将防重令牌存到服务器
        redisTemplate.opsForValue().set(OrderConstant.USER_ORDER_TOKEN + userLoginVo.getId(),token,30, TimeUnit.MINUTES);

        return  confirmVo;
    }


    /**
     * 本地事务：在分布式系统中，只能控制自己的回滚，控制不了其他服务的回滚；
     * 分布式事务：分布式事务应用的最大场景：网络问题；
     * Springboot中同一个对象内事务方法互调默认会失效，原因是绕过了代理对象，直接将代码粘贴到对应位置；
     * 解决：
     *  1. 引入spring-boot-starter-aop,引入AspectJ;
     *  2. 开启AspectJ动态代理；
     *  3. 以后使用代理对象进行互调；
     * @param orderSubmitVo
     * @return
     */
    @Override
//    @GlobalTransactional
    @Transactional
    public OrderSubmitResponseVo submitOrder(OrderSubmitVo orderSubmitVo) {
        threadLocal.set(orderSubmitVo);
        OrderSubmitResponseVo responseVo = new OrderSubmitResponseVo();
        responseVo.setCode(0);
        String script = "if redis.call('get',KEYS[1]) == ARGV[1] then return redis.call('del',KEYS[1]) else return 0 end";
        /*
         * 下单功能：创建订单->验价格->锁库存
         * ->支付选择页->下单失败，重新确认下单信息；
         */

        //1.验证令牌
        String orderToken = orderSubmitVo.getOrderToken();
        String queryToken = redisTemplate.opsForValue().get(OrderConstant.USER_ORDER_TOKEN + LoginInterceptor.threadLocal.get().getId());
        // 1.1 令牌验证通过 => 令牌的对比和删除必须原子性操作 = > 使用lua脚本实现
        // return 0 - 校验失败； return 1 - 删除成功
        Long execute = redisTemplate.execute(new DefaultRedisScript<Long>(script, Long.class), Arrays.asList(OrderConstant.USER_ORDER_TOKEN + LoginInterceptor.threadLocal.get().getId()), orderToken);
        if(execute == 1L){
            //1.1.1 令牌验证成功
            OrderCreateTo order = createOrder();
            BigDecimal payAmount = order.getOrderEntity().getPayAmount();
            BigDecimal payPrice = orderSubmitVo.getPayPrice();
            Double abs = Math.abs(payAmount.subtract(payPrice).doubleValue());
            if(abs <= 0.01){
                //金额对比成功
                //保存订单到数据库
                saveOrder(order);
                //库存锁定，出现异常，回滚订单数据; 订单号+订单项（skuId\skuNum\skuName）
                WareSkuLockVo wareSkuLockVo = new WareSkuLockVo();
                wareSkuLockVo.setOrderSn(order.getOrderEntity().getOrderSn());
                List<OrderItemVo> collect = order.getOrderItems().stream().map((item) -> {
                    OrderItemVo orderItemVo = new OrderItemVo();
                    orderItemVo.setSkuId(item.getSkuId());
                    orderItemVo.setCount(item.getSkuQuantity());
                    orderItemVo.setTitle(item.getSkuName());
                    return orderItemVo;
                }).collect(Collectors.toList());
                wareSkuLockVo.setLocks(collect);
                //调用仓库微服务，进行锁定库存信息的入MQ队列操作；
                R r = repositoryFeign.orderLockStock(wareSkuLockVo);
                if(r.getCode() == 0){
                    //库存锁定成功
                    responseVo.setOrderEntity(order.getOrderEntity());
                    //订单创建成功，发送消息给mq延时队列，一旦超时，延时队列将消息发送给release队列，有监听器会对release队列中的内容进行处理；
                    rabbitTemplate.convertAndSend("order-event-exchange","order.create.order",order.getOrderEntity());
                    return responseVo;
                }else{
                    //库存锁定失败
                    responseVo.setCode(3);
                    throw new NoStockException();
//                    return responseVo;
                }
            }else{
                responseVo.setCode(2);
            }
        }else{
            //1.1.2 令牌验证不通过
            responseVo.setCode(1);
        }
        return responseVo;
    }

    @Override
    public OrderEntity getOrderByOrderSn(String orderSn) {

        OrderEntity orderEntity = this.getOne(new QueryWrapper<OrderEntity>().eq("order_sn", orderSn));
        return orderEntity;
    }

    @Override
    public void closeOrder(OrderEntity orderEntity) {
        //所有的消息都会放到release队列中，因此在关闭订单前，必须要查询当前订单的状态；
        OrderEntity order = this.getOne(new QueryWrapper<OrderEntity>().eq("order_sn", orderEntity.getOrderSn()));
        if(order.getStatus() == OrderStatusEnum.CREATE_NEW.getCode() ){
            //关单时，创建一个新的对象,因为原实体对象从信息队列中获取，30min时间内订单的信息会有所变化；
            OrderEntity order2 = new OrderEntity();
            order2.setStatus(OrderStatusEnum.CANCLED.getCode());
            order2.setId(order.getId());
            this.updateById(order2);
            OrderTo orderTo = new OrderTo();
            BeanUtils.copyProperties(order,orderTo);
            try {
                //保证消息发送出去，每发送一个消息，都进行日志记录，即在数据库中保存每一个消息的详细信息；
                //定期扫描数据库，将失败的消息发送一遍；
                rabbitTemplate.convertAndSend("order_event_exchange","order.released.other",orderTo);
            } catch (AmqpException e) {

            }
        }
    }

    /**
     * 获取订单的相关支付信息
     * @param orderSn
     * @return
     */
    @Override
    public PayVo getOrderPayInfo(String orderSn) {
        PayVo payVo = new PayVo();
        OrderEntity orderEntity = this.getOne(new QueryWrapper<OrderEntity>().eq("order_sn", orderSn));
        OrderItemEntity orderItem = orderItemService.getOne(new QueryWrapper<OrderItemEntity>().eq("order_sn", orderSn));
        //鉴于支付宝的付款信息，只能接收小数点后两位，因此需要对bigdecimal的支付总额进行截取处理，方式为向上取值；
        BigDecimal bigDecimal = orderEntity.getPayAmount().setScale(2, RoundingMode.UP);
        payVo.setTotal_amount(bigDecimal.toString());
        payVo.setOut_trade_no(orderEntity.getOrderSn());
        payVo.setSubject(orderItem.getSkuName());
        payVo.setBody(orderItem.getSpuName());
        return payVo;
    }

    /**
     * 查询分页订单数据，支付完成会跳后需要使用
     * @param params
     * @return
     */
    @Override
    public PageUtils queryPageWithItem(Map<String, Object> params) {
        String id = LoginInterceptor.threadLocal.get().getId();
        IPage<OrderEntity> page = this.page(new Query<OrderEntity>().getPage(params),new QueryWrapper<OrderEntity>().eq("member_id",id).orderByDesc("id"));
        List<OrderEntity> orderItemEntities = new ArrayList<>();
        page.getRecords().forEach((item)->{
            List<OrderItemEntity> order_sn = orderItemService.list(new QueryWrapper<OrderItemEntity>().eq("order_sn", item.getOrderSn()));
            item.setOrderItemEntities(order_sn);
            orderItemEntities.add(item);
        });
        page.setRecords(orderItemEntities);
        return new PageUtils(page);
    }

    /**
     * 支付宝异步通知支付结果
     * @param vo
     * @return
     */
    @Override
    public String handleOutcome(PayAsyncVo vo) {
        // 1. 保存交易流水
        PaymentInfoEntity paymentInfoEntity = new PaymentInfoEntity();
        paymentInfoEntity.setAlipayTradeNo(vo.getTrade_no());
        paymentInfoEntity.setOrderSn(vo.getOut_trade_no());
        paymentInfoEntity.setPaymentStatus(vo.getTrade_status());
        paymentInfoEntity.setCallbackTime(vo.getNotify_time());
        paymentInfoService.save(paymentInfoEntity);
        //2. 修改订单状态
        String status = vo.getTrade_status();
        if(status.equals("TRADE_SUCCESS") || status.equals("TRADE_FINISHED")){
            this.baseMapper.updateOrder(vo.getOut_trade_no(),OrderStatusEnum.PAYED.getCode());
        }
        return "success";
    }

    @Override
    public void createSeckillOrder(SeckillOrderTo seckillOrderTo) {
        //1. 保存订单信息
        OrderEntity orderEntity = new OrderEntity();
        orderEntity.setOrderSn(seckillOrderTo.getOrderSn());
        orderEntity.setMemberId(Long.parseLong(seckillOrderTo.getUserId()));
        orderEntity.setStatus(OrderStatusEnum.CREATE_NEW.getCode());
        BigDecimal payment = seckillOrderTo.getSeckillCount().multiply(seckillOrderTo.getSeckillPrice());
        orderEntity.setPayAmount(payment);
        this.save(orderEntity);

        //2.保存订单项信息
        OrderItemEntity orderItemEntity = new OrderItemEntity();
        orderItemEntity.setOrderSn(seckillOrderTo.getOrderSn());
        orderItemEntity.setRealAmount(payment);
        orderItemEntity.setSkuQuantity(seckillOrderTo.getSeckillCount().intValue());
        R r = productFeign.getSpuInfoBySkuId(seckillOrderTo.getSkuId());
        SpuInfoVo data = r.getData(new TypeReference<SpuInfoVo>() {
        });
        orderItemEntity.setSpuBrand(data.getBrandId()+"");
        orderItemEntity.setSpuName(data.getSpuName());
        orderItemEntity.setSpuId(data.getId());
        orderItemService.save(orderItemEntity);
    }

    /**
     * 保存订单数据
     * @param order
     */
    private void saveOrder(OrderCreateTo order) {
        OrderEntity orderEntity = order.getOrderEntity();
        orderEntity.setModifyTime(new Date());
        //保存订单
        this.baseMapper.insert(orderEntity);
        List<OrderItemEntity> orderItems = order.getOrderItems();
        for (OrderItemEntity orderItem : orderItems) {
            orderItemService.save(orderItem);
        }
    }


    private OrderCreateTo createOrder(){
        OrderCreateTo orderCreateTo = new OrderCreateTo();
        OrderEntity orderEntity = new OrderEntity();
        //1. 生成订单号； 会员ID
        String order_sn = IdWorker.getTimeId();
        orderEntity.setOrderSn(order_sn);
        orderEntity.setMemberId(Long.parseLong(LoginInterceptor.threadLocal.get().getId()));
        //2. 获取收货地址信息
        R fare = repositoryFeign.getFare(threadLocal.get().getAddrId());
        FareResponseVo data = fare.getData(new TypeReference<FareResponseVo>() {
        });
        //2.1 运费金额
        BigDecimal dataFare = data.getFare();
        orderEntity.setFreightAmount(dataFare);
        //2.2 收货人地址信息
        orderEntity.setReceiverDetailAddress(data.getMemberAddressVo().getDetailAddress());
        orderEntity.setReceiverName(data.getMemberAddressVo().getName());
        orderEntity.setReceiverPhone(data.getMemberAddressVo().getPhone());
        orderEntity.setReceiverPostCode(data.getMemberAddressVo().getPostCode());
        orderEntity.setReceiverProvince(data.getMemberAddressVo().getProvince());
        orderEntity.setReceiverCity(data.getMemberAddressVo().getCity());
        orderEntity.setReceiverRegion(data.getMemberAddressVo().getRegion());
        //3. 订单项信息
        List<OrderItemEntity> orderItemEntities = buildOrderItems(order_sn);
        //4.验价
        computePrice(orderEntity,orderItemEntities);
        orderCreateTo.setOrderEntity(orderEntity);
        orderCreateTo.setOrderItems(orderItemEntities);
        return orderCreateTo;
    }

    private void computePrice(OrderEntity orderEntity, List<OrderItemEntity> orderItemEntities) {
        //1.订单价格
        BigDecimal totalPrice = new BigDecimal("0.0");
        BigDecimal promotion = new BigDecimal("0.0");
        BigDecimal coupon = new BigDecimal("0.0");
        BigDecimal integration = new BigDecimal("0.0");
        BigDecimal growth = new BigDecimal("0.0");
        BigDecimal gift = new BigDecimal("0.0");
        for (OrderItemEntity orderItemEntity : orderItemEntities) {
            totalPrice = totalPrice.add(orderItemEntity.getRealAmount());
            //获取各项优惠；
            BigDecimal couponAmount = orderItemEntity.getCouponAmount();
            BigDecimal integrationAmount = orderItemEntity.getIntegrationAmount();
            BigDecimal promotionAmount = orderItemEntity.getPromotionAmount();
            promotion = promotion.add(promotionAmount);
            coupon = coupon.add(couponAmount);
            integration = integration.add(integrationAmount);
            //积分、成长值
             growth = growth.add(new BigDecimal(orderItemEntity.getGiftGrowth().toString()));
             gift = gift.add(new BigDecimal(orderItemEntity.getGiftIntegration().toString()));
        }
        //设置各项总优惠
        orderEntity.setTotalAmount(totalPrice);
        orderEntity.setPayAmount(orderEntity.getTotalAmount().add(orderEntity.getFreightAmount()));
        orderEntity.setCouponAmount(coupon);
        orderEntity.setPromotionAmount(promotion);
        orderEntity.setIntegrationAmount(integration);
        //订单状态
        orderEntity.setStatus(OrderStatusEnum.CREATE_NEW.getCode());
        orderEntity.setConfirmStatus(15);
        //设置积分、成长值
        orderEntity.setGrowth(growth.intValue());
        orderEntity.setIntegration(integration.intValue());
        //订单删除状态； 0 - 未删除
        orderEntity.setDeleteStatus(0);
    }

    /**
     * 构建订单项集合
     * @return
     */
    private List<OrderItemEntity> buildOrderItems(String order_sn) {
        //最后确定每个购物项的价格；
        List<OrderItemVo> cartItems = cartFeign.getCartItems();
        List<OrderItemEntity> items = new ArrayList<>();
        if(cartItems != null && cartItems.size() > 0){
            cartItems.forEach((entity)->{
                OrderItemEntity orderItemEntity = buildOrderItem(entity);
                orderItemEntity.setOrderSn(order_sn);
                items.add(orderItemEntity);
            });
        }
        return  items;
    }

    /**
     * 构建每一个订单项
     * @param entity
     * @return
     */
    private OrderItemEntity buildOrderItem(OrderItemVo entity) {
        OrderItemEntity orderItemEntity = new OrderItemEntity();
        //1.订单信息：订单号
        //2.商品的spu信息；
        R r = productFeign.getSpuInfoBySkuId(entity.getSkuId());
        SpuInfoVo data = r.getData(new TypeReference<SpuInfoVo>() {
        });
        orderItemEntity.setSpuId(data.getId());
        orderItemEntity.setSpuName(data.getSpuName());
        orderItemEntity.setSpuBrand(data.getBrandId().toString());
        orderItemEntity.setCategoryId(data.getCatalogId());
        //3.商品的sku信息；
        orderItemEntity.setSkuId(entity.getSkuId());
        orderItemEntity.setSkuName(entity.getTitle());
        orderItemEntity.setSkuPic(entity.getDefaultImg());
        orderItemEntity.setSkuPrice(entity.getPrice());
        orderItemEntity.setSkuAttrsVals(StringUtils.collectionToDelimitedString(entity.getSkuAttr(),";"));
        orderItemEntity.setSkuQuantity(entity.getCount());
        //4.积分信息
        orderItemEntity.setGiftGrowth(entity.getPrice().intValue()*entity.getCount()/10);
        orderItemEntity.setGiftIntegration(entity.getPrice().intValue()*entity.getCount()/10);

        orderItemEntity.setPromotionAmount(new BigDecimal("0"));
        orderItemEntity.setCouponAmount(new BigDecimal("0"));
        orderItemEntity.setIntegrationAmount(new BigDecimal("0"));
        //当前订单项的实际金额
        orderItemEntity.setRealAmount(orderItemEntity.getSkuPrice().
                multiply(new BigDecimal(orderItemEntity.getSkuQuantity())).
                subtract(orderItemEntity.getPromotionAmount()).
                subtract(orderItemEntity.getCouponAmount()).
                subtract(orderItemEntity.getIntegrationAmount()));

        return orderItemEntity;
    }
}