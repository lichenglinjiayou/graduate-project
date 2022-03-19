package com.lichenglin.cart.vo;

import lombok.Data;

import java.math.BigDecimal;
import java.util.List;

/**
 * 购物车:需要计算的属性，应该重写get方法
 *
*/
public class Cart {

    List<CartItem> items;
    private Integer countNum; //商品总数量
    private Integer countType; //商品类型数量
    private BigDecimal totalAmount;
    private BigDecimal reduce = new BigDecimal(0); //减免价格

    public List<CartItem> getItems() {
        return items;
    }

    public void setItems(List<CartItem> items) {
        this.items = items;
    }

    public Integer getCountNum() {
        countNum = 0;
        if(items != null || items.size() > 0){
            items.forEach((item)->{
                countNum += item.getCount();
            });
        }
        return countNum;
    }

    public Integer getCountType() {
        countType = 0;
        if(items != null || items.size() > 0){
            items.forEach((item)->{
                countType += 1;
            });
        }
        return countType;
    }

    public BigDecimal getTotalAmount() {
        totalAmount = new BigDecimal(0);
        if(items != null || items.size() > 0){
            items.forEach((item)->{
                if(item.getCheck()){
                    totalAmount = totalAmount.add(item.getTotalPrice());
                }
            });
            totalAmount = totalAmount.subtract(getReduce());
        }
        return totalAmount;
    }

    public BigDecimal getReduce() {
        return reduce;
    }

    public void setReduce(BigDecimal reduce) {
        this.reduce = reduce;
    }
}
