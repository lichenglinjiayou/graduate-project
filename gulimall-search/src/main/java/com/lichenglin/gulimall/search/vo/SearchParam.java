package com.lichenglin.gulimall.search.vo;

import lombok.Data;

import java.util.List;

/**
 * 封装页面所有可能传来的条件
 */
@Data
public class SearchParam {
    //全文匹配的关键字
    private String keyword;
    //三级分类ID
    private Long catalog3Id;
    //排序条件：saleCount_asc/saleCount_desc ; skuPrice_asc/skuPrice_desc ; hotScore_asc/hotScore_desc ;
    private String sort;
    //过滤条件
    private Integer hasStock;
    //价格区间
    private String skuPrice;
    //品牌ID,支持多选；
    private List<Long> brandId;
    //按照属性进行过滤筛选
    //EXAMPLE: attrs=1_安卓：IOS & attrs=2_8G:16G
    private List<String> attrs;
    //分页
    private Integer pageNum = 1;

    private String _queryString;//原生查询条件
}
