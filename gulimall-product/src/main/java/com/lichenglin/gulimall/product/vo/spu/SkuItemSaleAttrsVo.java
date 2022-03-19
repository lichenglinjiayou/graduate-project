package com.lichenglin.gulimall.product.vo.spu;

import com.lichenglin.gulimall.product.vo.AttrValueWithSkuIdVo;
import lombok.Data;
import lombok.ToString;

import java.util.List;

@Data
@ToString
public class SkuItemSaleAttrsVo {
    private Long attrId;
    private String attrName;
    private List<AttrValueWithSkuIdVo> attrValues;
}