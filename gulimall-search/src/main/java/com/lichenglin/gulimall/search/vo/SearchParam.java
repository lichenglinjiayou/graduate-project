package com.lichenglin.gulimall.search.vo;

import lombok.Data;

import java.util.List;

/**
 * customized VO(value object) encapsulates all possible retrieval parameters
 */
@Data
public class SearchParam {
    // Keywords queried in page navigation bar
    private String keyword;
    // Three-level classification ID
    private Long catalog3Id;
    // Sort condition：saleCount_asc/saleCount_desc ; skuPrice_asc/skuPrice_desc ; hotScore_asc/hotScore_desc ;
    private String sort;
    // Stock information  0 - out of stock ; 1 - in stock
    private Integer hasStock;
    // Price range  XX_XX | XX_ | _XX
    private String skuPrice;
    // BrandId, support multiple selection
    private List<Long> brandId;
    // filter by product's attributes
    // EXAMPLE: attrs = 1_android:IOS & attrs=2_8G:16G
    private List<String> attrs;
    // pagination
    private Integer pageNum = 1;
    // query condition
    private String _queryString;
}
