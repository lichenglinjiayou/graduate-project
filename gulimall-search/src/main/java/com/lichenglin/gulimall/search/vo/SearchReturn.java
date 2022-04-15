package com.lichenglin.gulimall.search.vo;

import com.lichenglin.common.to.es.SkuEsModel;
import lombok.Data;

import java.util.ArrayList;
import java.util.List;

/**
 * Encapsulation of query result information
 */
@Data
public class SearchReturn {
    private List<SkuEsModel> products; // products' information from ElasticSearch
    private Integer pageNum; // current page
    private Integer pages; // total page
    private List<Integer> pageNav;
    private Long total; // total records
    private List<BrandVo> brands; // All brands involved in the current query result
    private List<AttrVo> attrs; // All attributes involved in the current query products
    private List<CatalogVo> catalogs; // All catalogs involved in the current query result
    private List<NavVo> navs = new ArrayList<>(); // Breadcrumbs
    private List<Long> attrIds = new ArrayList<>(); // record all unselected attributes

    @Data
    public static class NavVo{
        private String navName; // navigation name; such as: brand;
        private String navValue; // navigation value; such as: sumsang, apple;
        private String link; // navigation url address;
    }

    @Data
    public static class BrandVo{
        private Long brandId;
        private String brandName;
        private String brandImg;
    }

    @Data
    public static class AttrVo{
        private Long attrId;
        private String attrName;
        private List<String> attrValue;
    }

    @Data
    public static class CatalogVo{
        private Long catalogId;
        private String catalogName;
    }
}
