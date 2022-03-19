package com.lichenglin.cart.service.impl;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.TypeReference;
import com.lichenglin.cart.feign.ProductFeign;
import com.lichenglin.cart.interceptor.CartInterceptor;
import com.lichenglin.cart.service.CartService;
import com.lichenglin.cart.vo.*;
import com.lichenglin.common.constant.CartConstant;
import com.lichenglin.common.utils.R;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.BoundHashOperations;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.GetMapping;


import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.FutureTask;
import java.util.concurrent.ThreadPoolExecutor;

@Service
@Slf4j
public class CartServiceImpl implements CartService {

    @Autowired
    StringRedisTemplate stringRedisTemplate;

    @Autowired
    ProductFeign productFeign;

    @Autowired
    ThreadPoolExecutor threadPoolExecutor;



    @Override
    public CartItem addToCart(AddCommodityToCartVo addCommodityToCartVo) {
        BoundHashOperations<String, Object, Object> cartOps = getCartOps();
        String str = (String) cartOps.get(addCommodityToCartVo.getSkuId()+"");
        if(StringUtils.isEmpty(str)){
            /*
            添加新商品到购物车
            */
            CartItem cartItem = new CartItem();
            CompletableFuture<Void> getSkuInfoTask = CompletableFuture.runAsync(() -> {
                R r = productFeign.info(addCommodityToCartVo.getSkuId());
                SkuInfoVo skuInfo = r.getData("skuInfo", new TypeReference<SkuInfoVo>() {
                });
                cartItem.setCheck(true);
                cartItem.setCount(1);
                cartItem.setDefaultImg(skuInfo.getSkuDefaultImg());
                cartItem.setTitle(skuInfo.getSkuTitle());
                cartItem.setSkuId(addCommodityToCartVo.getSkuId());
                cartItem.setPrice(skuInfo.getPrice());
            }, threadPoolExecutor);

            //远程查询sku组合信息
            CompletableFuture<Void> getSkuAttr = CompletableFuture.runAsync(() -> {
                List<String> skuAttrSale = productFeign.getSkuAttrSale(addCommodityToCartVo.getSkuId());
                cartItem.setSkuAttr(skuAttrSale);
            }, threadPoolExecutor);

            CompletableFuture<Void> allTask = CompletableFuture.allOf(getSkuAttr, getSkuInfoTask);
            allTask.join();
            //购物项序列化为JSON字符串存放
            String cart = JSON.toJSONString(cartItem);
            cartOps.put(addCommodityToCartVo.getSkuId().toString(),cart);
            return cartItem;
        }else{
            /*
                购物车中有次商品
             */
            CartItem cartItem = JSON.parseObject(str, CartItem.class);
            Integer originNum = cartItem.getCount();
            cartItem.setCount(originNum + addCommodityToCartVo.getNum());
            String string = JSON.toJSONString(cartItem);
            cartOps.put(addCommodityToCartVo.getSkuId().toString(),string);
            return cartItem;
        }
    }

    /**
     * 获取购物车的某个购物项
     * @param skuId
     * @return
     */
    @Override
    public CartItem getCartItem(Long skuId) {
        BoundHashOperations cartOps = getCartOps();
        String str = (String) cartOps.get(skuId+"");
        CartItem cartItem = JSON.parseObject(str, CartItem.class);
        return cartItem;
    }

    @Override
    public Cart getCart() {
        UserInfoVo userInfoVo = CartInterceptor.threadLocal.get();
        Cart cart = new Cart();
        if(userInfoVo.getUserId() != null){
            //登录： 1. 合并购物车数据； 2. 从登录购物车获取数据
            String userKey = CartConstant.CART_PREFIX + userInfoVo.getUserId();
            BoundHashOperations<String, Object, Object> ops = stringRedisTemplate.boundHashOps(userKey);
            List<CartItem> tempCartItems = getCartItems(CartConstant.CART_PREFIX + userInfoVo.getUserKey());
            //合并购物车
            if(tempCartItems != null && tempCartItems.size() > 0){
                tempCartItems.forEach((item)->{
                    addToCart(new AddCommodityToCartVo(item.getSkuId(),item.getCount()));
                });
            }
            clearCart(userInfoVo.getUserKey());
            //获取登录后的购物车数据
            List<CartItem> cartItems = getCartItems(userKey);
            cart.setItems(cartItems);
        }else{
            // 未登录
            String userKey = CartConstant.CART_PREFIX + userInfoVo.getUserKey();
            List<CartItem> cartItems = getCartItems(userKey);
            cart.setItems(cartItems);
        }
        return cart;
    }

    @Override
    public void clearCart(String userKey) {
        userKey = CartConstant.CART_PREFIX + userKey;
        stringRedisTemplate.delete(userKey);
    }

    /*
        勾选购物项
     */
    @Override
    public void checkItem(Long skuId, Integer check) {
        CartItem cartItem = getCartItem(skuId);
        cartItem.setCheck(check == 1 ? true : false);
        String string = JSON.toJSONString(cartItem);
        BoundHashOperations<String, Object, Object> cartOps = getCartOps();
        cartOps.put(skuId.toString(),string);
    }

    /*
        改变商品数量
     */
    @Override
    public void changeItemCount(Long skuId, Integer num) {
        CartItem cartItem = getCartItem(skuId);
        cartItem.setCount(num);
        String string = JSON.toJSONString(cartItem);
        BoundHashOperations<String, Object, Object> cartOps = getCartOps();
        cartOps.put(skuId.toString(),string);
    }

    /*
        删除购物项
     */
    @Override
    public void deleteItem(Long skuId) {
        BoundHashOperations<String, Object, Object> cartOps = getCartOps();
        cartOps.delete(skuId.toString());
    }

    @Override
    public List<CartItem> getCartItemsByCurrentUser() {
        String userId = CartInterceptor.threadLocal.get().getUserId();
        if(userId == null){
            return null;
        }else{
            String s = CartConstant.CART_PREFIX + userId;
            List<CartItem> cartItems = getCartItems(s);
            List<CartItem> result = new ArrayList<>();
            cartItems.forEach((item)->{
                if(item.getCheck()){
                    //更新为最新价格
                    R price = productFeign.getPrice(item.getSkuId());
                    item.setPrice(price.getData("data",new TypeReference<BigDecimal>(){}));
                    result.add(item);
                }
            });
            return result;
        }

    }

    /*
        获取要操作的购物车
     */
    private BoundHashOperations<String, Object, Object> getCartOps() {
        UserInfoVo userInfoVo = CartInterceptor.threadLocal.get();
        String cartKey = "";
        if (userInfoVo.getUserId()!= null) {
            cartKey = CartConstant.CART_PREFIX + userInfoVo.getUserId();
        } else {
            cartKey = CartConstant.CART_PREFIX + userInfoVo.getUserKey();
        }

        //判断当前购物车是否有商品，如果有商品，则改变数量，否则增加商品
        BoundHashOperations<String, Object, Object> hashOps = stringRedisTemplate.boundHashOps(cartKey);
        return hashOps;
    }

    private List<CartItem> getCartItems(String userKey){
        BoundHashOperations<String, Object, Object> ops = stringRedisTemplate.boundHashOps(userKey);
        List<Object> values = ops.values();
        List<CartItem> list = new ArrayList<>();
        if(values != null && values.size() > 0){
            values.forEach((item)->{
                CartItem cartItem = JSON.parseObject((String) item, CartItem.class);
                list.add(cartItem);
            });
            return list;
        }
        return null;
    }


}
