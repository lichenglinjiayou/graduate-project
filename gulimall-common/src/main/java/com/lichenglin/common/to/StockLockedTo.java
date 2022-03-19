package com.lichenglin.common.to;

import lombok.Data;

import java.util.List;

@Data
public class StockLockedTo {

    private Long id; // 库存工作单
    private StockDetailTo detail; //工作单详情；


}
