package com.lichenglin.gulimall.search.service.impl;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.TypeReference;
import com.lichenglin.common.to.es.SkuEsModel;
import com.lichenglin.common.utils.R;
import com.lichenglin.gulimall.search.config.GuilMallElasticSearchConfig;
import com.lichenglin.gulimall.search.constant.EsConstant;
import com.lichenglin.gulimall.search.feign.ProductFeignService;
import com.lichenglin.gulimall.search.service.MallSearchService;
import com.lichenglin.gulimall.search.vo.AttrResponseWithPath;
import com.lichenglin.gulimall.search.vo.BrandVo;
import com.lichenglin.gulimall.search.vo.SearchParam;
import com.lichenglin.gulimall.search.vo.SearchReturn;
import org.apache.commons.lang.StringUtils;
import org.apache.lucene.search.join.ScoreMode;
import org.elasticsearch.action.search.SearchRequest;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.client.RestHighLevelClient;
import org.elasticsearch.index.query.*;
import org.elasticsearch.search.SearchHit;
import org.elasticsearch.search.SearchHits;
import org.elasticsearch.search.aggregations.AggregationBuilders;
import org.elasticsearch.search.aggregations.Aggregations;
import org.elasticsearch.search.aggregations.bucket.nested.NestedAggregationBuilder;
import org.elasticsearch.search.aggregations.bucket.nested.ParsedNested;
import org.elasticsearch.search.aggregations.bucket.terms.ParsedLongTerms;
import org.elasticsearch.search.aggregations.bucket.terms.ParsedStringTerms;
import org.elasticsearch.search.aggregations.bucket.terms.TermsAggregationBuilder;
import org.elasticsearch.search.builder.SearchSourceBuilder;
import org.elasticsearch.search.fetch.subphase.highlight.HighlightBuilder;
import org.elasticsearch.search.fetch.subphase.highlight.HighlightField;
import org.elasticsearch.search.sort.SortOrder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicReference;

@Service
public class MallSearchServiceImpl implements MallSearchService {

    @Autowired
    RestHighLevelClient restHighLevelClient;
    @Autowired
    ProductFeignService productFeignService;
    @Override
    public SearchReturn search(SearchParam searchParam) {
        //1. 准备检索请求
        SearchReturn result = null;
        SearchRequest searchRequest = null;
        try {
            searchRequest = buildSearchRequest(searchParam);
            //2.执行检索请求
            SearchResponse search = restHighLevelClient.search(searchRequest, GuilMallElasticSearchConfig.COMMON_OPTIONS);
            //3.分析响应数据并且封装成需要的格式
            result = buildSearchReturn(search,searchParam);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return result;
    }

    private SearchReturn buildSearchReturn(SearchResponse response,SearchParam searchParam) {
        SearchReturn searchReturn = new SearchReturn();
        SearchHits hits = response.getHits();
        //所有查询到的商品
        SearchHit[] hits1 = hits.getHits();
        List<SkuEsModel> products = new ArrayList<>();
        if(hits1 != null && hits1.length > 0){
            for (SearchHit hit : hits1) {
                String strJson = hit.getSourceAsString();
                SkuEsModel skuEsModel = JSON.parseObject(strJson, SkuEsModel.class);
                //设置标题为高亮字段
                if(!StringUtils.isEmpty(searchParam.getKeyword())){
                    HighlightField skuTitle = hit.getHighlightFields().get("skuTitle");
                    String highLightField = skuTitle.getFragments()[0].string();
                    skuEsModel.setSkuTitle(highLightField);
                }
                products.add(skuEsModel);
            }
        }
        searchReturn.setProducts(products);
        //商品的属性信息
        List<SearchReturn.AttrVo> attrVos = new ArrayList<>();
        ParsedNested attr_agg = response.getAggregations().get("attr_agg");
        ParsedLongTerms attr_id_agg = attr_agg.getAggregations().get("attr_id_agg");
        attr_id_agg.getBuckets().forEach((item)->{
            SearchReturn.AttrVo attrVo = new SearchReturn.AttrVo();
            attrVo.setAttrId(Long.parseLong(item.getKeyAsString()));
            ParsedStringTerms attr_name_agg = item.getAggregations().get("attr_name_agg");
            String attr_name = attr_name_agg.getBuckets().get(0).getKeyAsString();
            attrVo.setAttrName(attr_name);
            ParsedStringTerms attr_value_agg = item.getAggregations().get("attr_value_agg");
            List<String> attrValues = new ArrayList<>();
            attr_value_agg.getBuckets().forEach((bucket)->{
                String attrValue = bucket.getKeyAsString();
                attrValues.add(attrValue);
            });
            attrVo.setAttrValue(attrValues);
            attrVos.add(attrVo);
        });
        searchReturn.setAttrs(attrVos);
        //商品的品牌信息
        List<SearchReturn.BrandVo> brandVoList = new ArrayList<>();
        ParsedLongTerms brand_agg = response.getAggregations().get("brand_agg");
        brand_agg.getBuckets().forEach((item)->{
            SearchReturn.BrandVo brandVo = new SearchReturn.BrandVo();
            brandVo.setBrandId(Long.parseLong(item.getKeyAsString()));
            Aggregations aggregations = item.getAggregations();
            ParsedStringTerms brand_name_agg = aggregations.get("brand_name_agg");
            String brandName = brand_name_agg.getBuckets().get(0).getKeyAsString();
            brandVo.setBrandName(brandName);
            ParsedStringTerms brand_img_agg = aggregations.get("brand_img_agg");
            String img = brand_img_agg.getBuckets().get(0).getKeyAsString();
            brandVo.setBrandImg(img);
            brandVoList.add(brandVo);
        });
        searchReturn.setBrands(brandVoList);
        //商品的分类信息
        Aggregations aggregations = response.getAggregations();
        ParsedLongTerms catalog_agg = aggregations.get("catalog_agg");
        List<SearchReturn.CatalogVo> catalogVoList = new ArrayList<>();
        catalog_agg.getBuckets().forEach((item)->{
            SearchReturn.CatalogVo catalogVo = new SearchReturn.CatalogVo();
            catalogVo.setCatalogId(Long.parseLong(item.getKeyAsString()));
            ParsedStringTerms catalog_name_agg = item.getAggregations().get("catalog_name_agg");
            catalogVo.setCatalogName(catalog_name_agg.getBuckets().get(0).getKeyAsString());
            catalogVoList.add(catalogVo);
        });
        searchReturn.setCatalogs(catalogVoList);
    //===============================
        //商品的分页信息
        Integer pageNum = searchParam.getPageNum();
        searchReturn.setPageNum(pageNum);
        //商品的总记录数
        long total = hits.getTotalHits().value;
        searchReturn.setTotal(total);
        //商品的总页码
        Integer pages = (int)total % EsConstant.PAGE_SIZE == 0 ? (int)total/EsConstant.PAGE_SIZE : (int)total/EsConstant.PAGE_SIZE+1;
        searchReturn.setPages(pages);

        List<Integer> pageNav = new ArrayList<>();
        //导航页码
        for (Integer i = 1; i <= pages; i++) {
            pageNav.add(i);
        }
        searchReturn.setPageNav(pageNav);


        // 添加面包屑导航功能；
        List<SearchReturn.NavVo> list = new ArrayList<>();
        List<String> attrs = searchParam.getAttrs();
        if(attrs != null && attrs.size() > 0){
            searchParam.getAttrs().forEach((item)->{
                SearchReturn.NavVo navVo = new SearchReturn.NavVo();
                String[] s = item.split("_");
                navVo.setNavValue(s[1]);
                R r = productFeignService.attrInfo(Long.parseLong(s[0]));
                    if((Integer)r.get("code") == 0){
                    AttrResponseWithPath attr = r.getData("attr", new TypeReference<AttrResponseWithPath>() {
                    });
                    navVo.setNavName(attr.getAttrName());
                        try {
                            String replace = replaceQueryString(searchParam, item, s,"attrs");
                            navVo.setLink("http://search.gulimall.com/list.html?"+replace);
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }else{
                    navVo.setNavName(s[0]);
                }
                list.add(navVo);
            });
            //取消面包屑，跳转的地址；将请求地址的当前条件置换掉
            searchReturn.setNavs(list);
        }
//        if(searchParam.getBrandId() != null && searchParam.getBrandId().size() > 0){
//            List<SearchReturn.NavVo> navs = searchReturn.getNavs();
//            SearchReturn.NavVo navVo = new SearchReturn.NavVo();
//            navVo.setNavName("品牌");
//            R r = productFeignService.brandInfo(searchParam.getBrandId());
//            if((Integer) r.get("code") == 0){
//                List<BrandVo> brand = r.getData("brand", new TypeReference<List<BrandVo>>() {});
//                StringBuffer sb = new StringBuffer();
//                AtomicReference<String> replace = new AtomicReference<>("");
//                brand.forEach((item)->{
//                   sb.append(item.getBrandName()).append(";");
//                    try {
//                        String encode = URLEncoder.encode(item.getBrandId() + "", "UTF-8");
//                        replace.set(searchParam.get_queryString().replace("&brandId=" + encode, ""));
//                        navVo.setLink("http://search.gulimall.com/list.html?"+replace);
//                    } catch (UnsupportedEncodingException e) {
//                        e.printStackTrace();
//                    }
//                });
//                navVo.setNavValue(sb.toString());
//            }
//
//            navs.add(navVo);
//        }


        return searchReturn;
    }

    private String replaceQueryString(SearchParam searchParam, String item, String[] s,String key) throws UnsupportedEncodingException {
        String[] split = s[1].split(";");
        String encode = null;
        if(split.length == 1){
            encode = URLEncoder.encode(item, "UTF-8");
        } else{
            StringBuilder sb = new StringBuilder();
            for (int i = 0; i < split.length-1; i++) {
                String str = URLEncoder.encode(split[i], "UTF-8");
                sb.append(str).append(";");
            }
            sb.append(URLEncoder.encode(split[split.length-1],"UTF-8"));
            encode = s[0] + "_" +sb.toString();
        }
        return searchParam.get_queryString().replace("&"+ key +"=" + encode, "");
    }

    private SearchRequest buildSearchRequest(SearchParam searchParam) {
        //通过SearchSourceBuilder构建DSL检索语句
        SearchSourceBuilder source = new SearchSourceBuilder();
        //1.模糊匹配，过滤
        BoolQueryBuilder boolQueryBuilder = QueryBuilders.boolQuery();
        //1.1 must
        if(!StringUtils.isEmpty(searchParam.getKeyword())){
            boolQueryBuilder.must(QueryBuilders.matchQuery("skuTitle",searchParam.getKeyword()));
        }
        //1.2 filter
        //按照三级分类ID
        if(searchParam.getCatalog3Id() != null){
            boolQueryBuilder.filter(QueryBuilders.termQuery("catalogId",searchParam.getCatalog3Id()));
        }
        //按照品牌ID
        if(searchParam.getBrandId() != null && searchParam.getBrandId().size() > 0){
            boolQueryBuilder.filter(QueryBuilders.termsQuery("brandId",searchParam.getBrandId()));
        }
        //按照指定属性进行查询；
        if(searchParam.getAttrs() != null && searchParam.getAttrs().size() > 0){
            List<String> attrs = searchParam.getAttrs();
            attrs.forEach((item)->{
                BoolQueryBuilder boolQuery = QueryBuilders.boolQuery();
                String[] s = item.split("_");
                String attrId = s[0];
                String[] attrValue = s[1].split(":");
                boolQuery.must(QueryBuilders.termQuery("attrs.attrId",s[0]));
                boolQuery.must(QueryBuilders.termsQuery("attrs.attrValue",attrValue));
                NestedQueryBuilder nestedQuery = QueryBuilders.nestedQuery("attrs",boolQuery, ScoreMode.None);
                boolQueryBuilder.filter(nestedQuery);
            });
        }
        //按照库存进行查询
        if(searchParam.getHasStock() != null){
            boolQueryBuilder.filter(QueryBuilders.termQuery("hasStock",searchParam.getHasStock() == 1?true:false));
        }
        //按照价格区间进行检索
        if(!StringUtils.isEmpty(searchParam.getSkuPrice())){
            String[] s = searchParam.getSkuPrice().split("_");
            RangeQueryBuilder skuPrice = QueryBuilders.rangeQuery("skuPrice");
            if(s.length == 2){
                skuPrice.gte(s[0]).lte(s[1]);
            }else if(s.length == 1){
                if(searchParam.getSkuPrice().charAt(0) == '_'){
                    skuPrice.lte(s[0]);
                }else{
                    skuPrice.gte(s[0]);
                }
            }
            boolQueryBuilder.filter(skuPrice);
        }

        source.query(boolQueryBuilder);
        //2.排序、分页、高亮
        //2.1 排序
        if(!StringUtils.isEmpty(searchParam.getSort())){
            String sort = searchParam.getSort();
            String[] s = sort.split("_");
            String sortField = s[0];
            String sortMethod = s[1];
            source.sort(sortField,sortMethod.equalsIgnoreCase("asc") ? SortOrder.ASC:SortOrder.DESC);
        }
        //2.2 分页
        source.from((searchParam.getPageNum()-1)*EsConstant.PAGE_SIZE);
        source.size(EsConstant.PAGE_SIZE);
        //2.3 高亮
        if(!StringUtils.isEmpty(searchParam.getKeyword())){
            HighlightBuilder highlightBuilder = new HighlightBuilder();
            highlightBuilder.field("skuTitle");
            highlightBuilder.preTags("<b style='color:red'>");
            highlightBuilder.postTags("</b>");
            source.highlighter(highlightBuilder);
        }
        //3. 聚合分析
        //3.1 品牌聚合
        TermsAggregationBuilder brandAgg = AggregationBuilders.terms("brand_agg").field("brandId").size(5)
                .subAggregation(AggregationBuilders.terms("brand_name_agg").field("brandName").size(1))
                .subAggregation(AggregationBuilders.terms("brand_img_agg").field("brandImg").size(1));
        source.aggregation(brandAgg);
        //3.2 分类聚合
        source.aggregation(AggregationBuilders.terms("catalog_agg").field("catalogId").size(5)
        .subAggregation(AggregationBuilders.terms("catalog_name_agg").field("catelogName").size(1)));
        //3.3 属性聚合
        NestedAggregationBuilder attr_agg = AggregationBuilders.nested("attr_agg", "attrs");
        TermsAggregationBuilder attr_id_agg = AggregationBuilders.terms("attr_id_agg").field("attrs.attrId").size(5);
        TermsAggregationBuilder attr_name_agg = AggregationBuilders.terms("attr_name_agg").field("attrs.attrName").size(1);
        TermsAggregationBuilder attr_value_agg = AggregationBuilders.terms("attr_value_agg").field("attrs.attrValue").size(10);
        attr_id_agg.subAggregation(attr_name_agg);
        attr_id_agg.subAggregation(attr_value_agg);
        attr_agg.subAggregation(attr_id_agg);
        source.aggregation(attr_agg);

        System.out.println(source.toString());
        SearchRequest request = new SearchRequest(new String[]{EsConstant.PRODUCT_INDEX}, source);
        return request;
    }
}
