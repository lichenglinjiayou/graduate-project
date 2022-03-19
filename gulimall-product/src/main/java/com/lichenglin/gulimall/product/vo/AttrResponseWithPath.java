package com.lichenglin.gulimall.product.vo;

import lombok.Data;

import java.util.List;

@Data
public class AttrResponseWithPath extends AttrVo {
    private List<Long> catelogPath;
    private String attrGroupName;
    private Long attrGroupId;
}
