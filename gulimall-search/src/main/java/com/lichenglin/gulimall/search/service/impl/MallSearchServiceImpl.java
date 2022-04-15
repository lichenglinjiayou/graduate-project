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
    // Automatically inject interfaces that call remote product services;
    @Autowired
    ProductFeignService productFeignService;

    // DSL statements required for building queries
    @Override
    public SearchReturn search(SearchParam searchParam) {
        SearchReturn result = null;
        //1. prepare search requests
        SearchRequest searchRequest = null;
        try {
            // 1.1 Extract the building retrieval request into a separate method : buildSearchRequest()
            searchRequest = buildSearchRequest(searchParam);
            //2. execute search requests
            SearchResponse search = restHighLevelClient.search(searchRequest, GuilMallElasticSearchConfig.COMMON_OPTIONS);
            //3. analyse response data and encapsulate them into a stable format
            result = buildSearchReturn(search, searchParam);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return result;
    }

    private SearchReturn buildSearchReturn(SearchResponse response, SearchParam searchParam) {
        SearchReturn searchReturn = new SearchReturn();
        // hits contains all queried products' information
        SearchHits hits = response.getHits();
        // 1. products - encapsulate all queried products' information in the result object;
        SearchHit[] hits1 = hits.getHits();
        // encapsulate all search result;
        List<SkuEsModel> products = new ArrayList<>();
        if (hits1 != null && hits1.length > 0) {
            // 1.1 iterator each hit record
            for (SearchHit hit : hits1) {
                // 1.2 all relevant information is encapsilated in the field '_source'; => obtain '_source'
                String strJson = hit.getSourceAsString();
                // 1.3 convert JSON string to object;
                SkuEsModel skuEsModel = JSON.parseObject(strJson, SkuEsModel.class);
                // 1.4 if query with retrieval conditions, it will be highlighted;
                if (!StringUtils.isEmpty(searchParam.getKeyword())) {
                    // 1.5 Get what needs to be highlighted
                    HighlightField skuTitle = hit.getHighlightFields().get("skuTitle");
                    String highLightField = skuTitle.getFragments()[0].string();
                    // 1.6 set highLightField;
                    skuEsModel.setSkuTitle(highLightField);
                }
                products.add(skuEsModel);
            }
        }
        searchReturn.setProducts(products);
        // 2. attrs - encapsulate products' attribute information
        List<SearchReturn.AttrVo> attrVos = new ArrayList<>();
        ParsedNested attr_agg = response.getAggregations().get("attr_agg");
        // 2.1 get subAggregation information - "attr_id_agg";
        ParsedLongTerms attr_id_agg = attr_agg.getAggregations().get("attr_id_agg");
        // 2.2 buckets contain all brand information, so iterator bucket[];
        attr_id_agg.getBuckets().forEach((item) -> {
            // 2.3 create AttrVo Object,and use this object to encapsulate outcome data;
            SearchReturn.AttrVo attrVo = new SearchReturn.AttrVo();
            // 2.3.1 obtain attrId;
            attrVo.setAttrId(Long.parseLong(item.getKeyAsString()));
            // get subAggregation information - "attr_name_agg";
            ParsedStringTerms attr_name_agg = item.getAggregations().get("attr_name_agg");
            // 2.3.2 obtain attrName;
            String attr_name = attr_name_agg.getBuckets().get(0).getKeyAsString();
            attrVo.setAttrName(attr_name);
            // get subAggregation information - "attr_value_agg";
            ParsedStringTerms attr_value_agg = item.getAggregations().get("attr_value_agg");
            // 2.3.3 attrValue may contains multiple results; use List<String>
            List<String> attrValues = new ArrayList<>();
            attr_value_agg.getBuckets().forEach((bucket) -> {
                // 2.3.4 obtain all attrValue and  add it into the List;
                String attrValue = bucket.getKeyAsString();
                attrValues.add(attrValue);
            });
            attrVo.setAttrValue(attrValues);
            // 2.4 add object to collection;
            attrVos.add(attrVo);
        });
        // 2.5 add the collection to the result object;
        searchReturn.setAttrs(attrVos);
        // 3. brands - ancapsulate products' brand information
        List<SearchReturn.BrandVo> brandVoList = new ArrayList<>();
        ParsedLongTerms brand_agg = response.getAggregations().get("brand_agg");
        // 3.1 buckets contain all brand information, so iterator bucket[];
        brand_agg.getBuckets().forEach((item) -> {
            // 3.2 create brandVo Object,and use this object to encapsulate outcome data;
            SearchReturn.BrandVo brandVo = new SearchReturn.BrandVo();
            // 3.2.1 obtain brandId;
            brandVo.setBrandId(Long.parseLong(item.getKeyAsString()));
            // get subAggregation information - "brand_name_agg";
            Aggregations aggregations = item.getAggregations();
            ParsedStringTerms brand_name_agg = aggregations.get("brand_name_agg");
            // 3.2.2 obtain brandName;
            String brandName = brand_name_agg.getBuckets().get(0).getKeyAsString();
            brandVo.setBrandName(brandName);
            // get subAggregation information - "brand_img_agg";
            ParsedStringTerms brand_img_agg = aggregations.get("brand_img_agg");
            // 3.2.3 obtain brandImg;
            String img = brand_img_agg.getBuckets().get(0).getKeyAsString();
            brandVo.setBrandImg(img);
            // 3.3 add object to collection;
            brandVoList.add(brandVo);
        });
        // 3.4 add the collection to the result object;
        searchReturn.setBrands(brandVoList);
        // 4. catalogs - ancapsulate products' catalog information
        Aggregations aggregations = response.getAggregations();
        ParsedLongTerms catalog_agg = aggregations.get("catalog_agg");
        List<SearchReturn.CatalogVo> catalogVoList = new ArrayList<>();
        // 4.1 buckets contain all classification information, so iterator bucket[];
        catalog_agg.getBuckets().forEach((item) -> {
            // 4.2 create CatalogVo Object,and use this object to encapsulate outcome;
            SearchReturn.CatalogVo catalogVo = new SearchReturn.CatalogVo();
            // 4.2.1 obtain catalogId;
            catalogVo.setCatalogId(Long.parseLong(item.getKeyAsString()));
            // get subAggregation information;
            ParsedStringTerms catalog_name_agg = item.getAggregations().get("catalog_name_agg");
            // 4.2.2 obtain catalogName;
            catalogVo.setCatalogName(catalog_name_agg.getBuckets().get(0).getKeyAsString());
            // 4.3 add object to collection;
            catalogVoList.add(catalogVo);
        });
        // 4.4 add the collection to the result object;
        searchReturn.setCatalogs(catalogVoList);
        //===============================
        // 5. pagination  =>  obtain current pageNum from request's parameters
        Integer pageNum = searchParam.getPageNum();
        searchReturn.setPageNum(pageNum);
        // 6. total records =>  hit.total.value = total records
        long total = hits.getTotalHits().value;
        searchReturn.setTotal(total);
        // 7. total pages => total records / PAGE_SIZE (if records mod PAGE_SIZE == 0 ? records / PAGE_SIZE : records / PAGE_SIZE+1)
        Integer pages = (int) total % EsConstant.PAGE_SIZE == 0 ? (int) total / EsConstant.PAGE_SIZE : (int) total / EsConstant.PAGE_SIZE + 1;
        searchReturn.setPages(pages);

        List<Integer> pageNav = new ArrayList<>();
        // 8. navigated page Number;
        for (Integer i = 1; i <= pages; i++) {
            pageNav.add(i);
        }
        searchReturn.setPageNav(pageNav);


        //  9. Add breadcrumb navigation
        List<SearchReturn.NavVo> list = new ArrayList<>();
        List<String> attrs = searchParam.getAttrs();
        if (attrs != null && attrs.size() > 0) {
            // 9.1 iterator query attrs;
            searchParam.getAttrs().forEach((item) -> {
                // 9.2 create NavVo object;
                SearchReturn.NavVo navVo = new SearchReturn.NavVo();
                // 9.3 split attr; s[0] => attrId  s[1] => attrValue
                String[] s = item.split("_");
                navVo.setNavValue(s[1]);
                // 9.4 remote call gulimall-product service, Query attribute name by attribute ID;
                searchReturn.getAttrIds().add(Long.parseLong(s[0]));
                R r = productFeignService.attrInfo(Long.parseLong(s[0]));
                // 9.5 r.get("code") == 0 => remote call success;
                if ((Integer) r.get("code") == 0) {
                    // copy AttrResponseWithPath object; use this object encapsulate data;
                    AttrResponseWithPath attr = r.getData("attr", new TypeReference<AttrResponseWithPath>() {
                    });
                    // 9,6 obtain attrName;
                    navVo.setNavName(attr.getAttrName()+":");
                    try {
                        String replace = replaceQueryString(searchParam, item, s, "attrs");
                        navVo.setLink("http://search.gulimall.com/list.html?" + replace);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                } else {
                    navVo.setNavName(s[0]+":");
                }
                list.add(navVo);
            });
            // 9.7 Cancel the breadcrumbs, the jumping address; replace the current condition of the request address
            searchReturn.setNavs(list);
        }
        if(searchParam.getBrandId() != null && searchParam.getBrandId().size() > 0){
            List<SearchReturn.NavVo> navs = searchReturn.getNavs();
            SearchReturn.NavVo navVo = new SearchReturn.NavVo();
            navVo.setNavName("Brand:");
            R r = productFeignService.brandInfo(searchParam.getBrandId());
            if((Integer) r.get("code") == 0){
                List<BrandVo> brand = r.getData("data", new TypeReference<List<BrandVo>>() {});
                StringBuffer sb = new StringBuffer();
                AtomicReference<String> replace = new AtomicReference<>("");
                brand.forEach((item)->{
                   sb.append(item.getName());
                    try {
                        String encode = URLEncoder.encode(item.getBrandId() + "", "UTF-8");
                        replace.set(searchParam.get_queryString().replace("&brandId=" + encode, ""));
                        navVo.setLink("http://search.gulimall.com/list.html?"+replace);
                    } catch (UnsupportedEncodingException e) {
                        e.printStackTrace();
                    }
                });
                navVo.setNavValue(sb.toString());
            }

            navs.add(navVo);
        }


        return searchReturn;
    }

    private String replaceQueryString(SearchParam searchParam, String item, String[] s, String key) throws UnsupportedEncodingException {
        String[] split = s[1].split(";");
        String encode = null;
        if (split.length == 1) {
            encode = URLEncoder.encode(item, "UTF-8");
        } else {
            StringBuilder sb = new StringBuilder();
            for (int i = 0; i < split.length - 1; i++) {
                String str = URLEncoder.encode(split[i], "UTF-8");
                sb.append(str).append(";");
            }
            sb.append(URLEncoder.encode(split[split.length - 1], "UTF-8"));
            encode = s[0] + "_" + sb.toString();
        }
        return searchParam.get_queryString().replace("&" + key + "=" + encode, "");
    }

    /**
     * establish DSL statements
     *
     * @param searchParam
     * @return
     */
    private SearchRequest buildSearchRequest(SearchParam searchParam) {
        // create SearchSourceBuilder Object;
        SearchSourceBuilder source = new SearchSourceBuilder();
        // 1. create boolQuey;
        BoolQueryBuilder boolQueryBuilder = QueryBuilders.boolQuery();
        // 1.1 If a keyword is passed, fuzzy matching is performed based on the keyword
        if (!StringUtils.isEmpty(searchParam.getKeyword())) {
            boolQueryBuilder.must(QueryBuilders.matchQuery("skuTitle", searchParam.getKeyword()));
        }
        // 1.2 filter
        // 1.2.1 according to catalogId3
        if (searchParam.getCatalog3Id() != null) {
            boolQueryBuilder.filter(QueryBuilders.termQuery("catalogId", searchParam.getCatalog3Id()));
        }
        // 1.2.2 according to brandId
        if (searchParam.getBrandId() != null && searchParam.getBrandId().size() > 0) {
            boolQueryBuilder.filter(QueryBuilders.termsQuery("brandId", searchParam.getBrandId()));
        }
        // 1.2.3 according to attrs
        if (searchParam.getAttrs() != null && searchParam.getAttrs().size() > 0) {
            List<String> attrs = searchParam.getAttrs();
            attrs.forEach((item) -> {
                String[] s = item.split("_");
                // s[0] == attrId;
                String attrId = s[0];
                // s[1] == attrValue; May contain multiple property values, separated by colons;
                String[] attrValue = s[1].split(":");
                // create boolQuery and encapsulate property conditions for boolQuery;
                BoolQueryBuilder boolQuery = QueryBuilders.boolQuery();
                boolQuery.must(QueryBuilders.termQuery("attrs.attrId", s[0]));
                boolQuery.must(QueryBuilders.termsQuery("attrs.attrValue", attrValue));
                // create nestedQuery;
                NestedQueryBuilder nestedQuery = QueryBuilders.nestedQuery("attrs", boolQuery, ScoreMode.None);
                // finally,assemble nestedQuery into the outermost boolQuery;
                boolQueryBuilder.filter(nestedQuery);
            });
        }
        // 1.2.4 according to stock
        if (searchParam.getHasStock() != null) {
            boolQueryBuilder.filter(QueryBuilders.termQuery("hasStock", searchParam.getHasStock() == 1 ? true : false));
        }
        // 1.2.5 according to price range
        if (!StringUtils.isEmpty(searchParam.getSkuPrice())) {
            // price range format : [xx]_[xx].
            // Firstly, split the string by '_'
            String[] s = searchParam.getSkuPrice().split("_");
            // create rangeQuery;
            RangeQueryBuilder skuPrice = QueryBuilders.rangeQuery("skuPrice");
            // judge if the length of array s is 2 , meaning price contains left and right borders;
            if (s.length == 2) {
                skuPrice.gte(s[0]).lte(s[1]);
            // if the length of array s is 1,meaning price contains left or right borders;
            } else if (s.length == 1) {
                // judge if the s[0] == '_', meaning price contains right borders;
                if (searchParam.getSkuPrice().charAt(0) == '_') {
                    skuPrice.lte(s[0]);
                } else {
                    // else price contains left borders;
                    skuPrice.gte(s[0]);
                }
            }
            boolQueryBuilder.filter(skuPrice);
        }

        source.query(boolQueryBuilder);
        // 2.sort、pagination、highlight
        // 2.1 sort
        if (!StringUtils.isEmpty(searchParam.getSort())) {
            String sort = searchParam.getSort();
            String[] s = sort.split("_");
            // s[0] = Field to be sorted
            String sortField = s[0];
            // s[1] = sort method : desc/asc;
            String sortMethod = s[1];
            source.sort(sortField, sortMethod.equalsIgnoreCase("asc") ? SortOrder.ASC : SortOrder.DESC);
        }
        //2.2 pagination
        source.from((searchParam.getPageNum() - 1) * EsConstant.PAGE_SIZE);
        source.size(EsConstant.PAGE_SIZE);
        //2.3 highlight(only pass keyword can trigger highlighting)
        if (!StringUtils.isEmpty(searchParam.getKeyword())) {
            HighlightBuilder highlightBuilder = new HighlightBuilder();
            // determine which field needed to be highlighted;
            highlightBuilder.field("skuTitle");
            // set prefix tag
            highlightBuilder.preTags("<b style='color:red'>");
            // sete post tag
            highlightBuilder.postTags("</b>");
            source.highlighter(highlightBuilder);
        }
        //3. Aggregate analysis of query results
        //3.1 Aggregate brand information
        TermsAggregationBuilder brandAgg = AggregationBuilders.terms("brand_agg").field("brandId").size(10)
                // subAggregate brandName
                .subAggregation(AggregationBuilders.terms("brand_name_agg").field("brandName").size(1))
                // subAggregate brandImg
                .subAggregation(AggregationBuilders.terms("brand_img_agg").field("brandImg").size(1));
        source.aggregation(brandAgg);
        //3.2 Aggregate catalog information
        source.aggregation(AggregationBuilders.terms("catalog_agg").field("catalogId").size(10)
                // subAggregate catelogName
                .subAggregation(AggregationBuilders.terms("catalog_name_agg").field("catelogName").size(1)));
        //3.3 Aggregate attribute information
        NestedAggregationBuilder attr_agg = AggregationBuilders.nested("attr_agg", "attrs");
        // NestedAggregation's subAggregation
        TermsAggregationBuilder attr_id_agg = AggregationBuilders.terms("attr_id_agg").field("attrs.attrId").size(10);
        // attr_id_agg's subAggregation - attrName
        TermsAggregationBuilder attr_name_agg = AggregationBuilders.terms("attr_name_agg").field("attrs.attrName").size(1);
        // attr_id_agg's subAggregation - attrValue(mulitiple value)
        TermsAggregationBuilder attr_value_agg = AggregationBuilders.terms("attr_value_agg").field("attrs.attrValue").size(10);
        // set subAggregation for attr_id_agg;
        attr_id_agg.subAggregation(attr_name_agg);
        attr_id_agg.subAggregation(attr_value_agg);
        // set subAggregation for attr_agg
        attr_agg.subAggregation(attr_id_agg);
        // set attr_agg as SearchSourceBuilder's condition;
        source.aggregation(attr_agg);
        System.out.println(source.toString());
        // establish DSL statement, parameter1 - index ; parameter2 - SearchSourceBuilder(help create DSL)
        SearchRequest request = new SearchRequest(new String[]{EsConstant.PRODUCT_INDEX}, source);
        return request;
    }
}
