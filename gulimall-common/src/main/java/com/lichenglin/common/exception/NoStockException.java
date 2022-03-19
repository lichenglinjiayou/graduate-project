package com.lichenglin.common.exception;


public class NoStockException extends   RuntimeException{
    private Long skuId;
    public NoStockException(Long skuId) {
        super(skuId+",没有足够的库存");
    }

    public NoStockException() {

    }

    public Long getSkuId() {
        return skuId;
    }

    public void setSkuId(Long skuId) {
        this.skuId = skuId;
    }
}
