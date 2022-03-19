package com.lichenglin.gulimall.repository.vo;

import lombok.Data;

import java.util.List;

@Data
public class PurchaseFinishVo {
    private Long id;
    private List<PurchaseDetailVo> items;
}
