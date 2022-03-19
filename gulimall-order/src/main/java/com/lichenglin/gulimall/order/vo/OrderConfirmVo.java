package com.lichenglin.gulimall.order.vo;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

//@Data
public class OrderConfirmVo {

    private List<MemberAddressVo> address;
    private List<OrderItemVo> items;
    //TODO:发票
    //积分
    private Integer integration;

    private BigDecimal totalPrice;

    private BigDecimal payPrice;

    private Map<Long,Boolean> stock;

    //防重令牌
    private String orderToken;


    public Integer getCount(){
        Integer i = 0;
        for (OrderItemVo item : items) {
            i += item.getCount();
        }
        return i;
    }

    public List<MemberAddressVo> getAddress() {
        return address;
    }

    public void setAddress(List<MemberAddressVo> address) {
        this.address = address;
    }

    public List<OrderItemVo> getItems() {
        return items;
    }

    public void setItems(List<OrderItemVo> items) {
        this.items = items;
    }

    public Integer getIntegration() {
        return integration;
    }

    public void setIntegration(Integer integration) {
        this.integration = integration;
    }

    public BigDecimal getTotalPrice() {
        if(items != null){
            totalPrice = new BigDecimal(0);
            items.forEach((item)->{
                BigDecimal price = item.getPrice().multiply(new BigDecimal(item.getCount()));
                totalPrice = totalPrice.add(price);
            });
        }
        return totalPrice;
    }


    public BigDecimal getPayPrice() {
        if(items != null){
            payPrice = new BigDecimal(0);
            items.forEach((item)->{
                BigDecimal price = item.getPrice().multiply(new BigDecimal(item.getCount()));
                payPrice = payPrice.add(price);
            });
        }
        return payPrice;
    }

    public Map<Long, Boolean> getStock() {
        return stock;
    }

    public void setStock(Map<Long, Boolean> stock) {
        this.stock = stock;
    }

    public String getOrderToken() {
        return orderToken;
    }

    public void setOrderToken(String orderToken) {
        this.orderToken = orderToken;
    }
}

