package com.lichenglin.gulimall.search.vo;

import com.lichenglin.common.to.es.SkuEsModel;
import lombok.Data;

import java.util.ArrayList;
import java.util.List;

/**
 * 返回页面的信息
 */
@Data
public class SearchReturn {
    private List<SkuEsModel> products; //ES检索出来的商品信息
    private Integer pageNum; //当前页码
    private Integer pages; //总页码
    private List<Integer> pageNav;
    private Long total; //总记录数
    private List<BrandVo> brands; //当前查询结果，所有涉及的品牌
    private List<AttrVo> attrs; //当前查询结果，所涉及的属性
    private List<CatalogVo> catalogs; //当前查询结果，所涉及的所有分类
    private List<NavVo> navs = new ArrayList<>(); //面包屑导航

    @Data
    public static class NavVo{
        private String navName;
        private String navValue;
        private String link;
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
