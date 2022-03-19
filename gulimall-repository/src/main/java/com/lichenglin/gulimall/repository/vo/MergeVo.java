package com.lichenglin.gulimall.repository.vo;

import lombok.Data;

import java.util.List;

@Data
public class MergeVo {

    private  Long purchaseId;
    private List<Long> items;
}
