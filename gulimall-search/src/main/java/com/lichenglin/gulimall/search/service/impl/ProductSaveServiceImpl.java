package com.lichenglin.gulimall.search.service.impl;

import com.alibaba.fastjson.JSON;
import com.lichenglin.common.to.es.SkuEsModel;
import com.lichenglin.gulimall.search.config.GuilMallElasticSearchConfig;
import com.lichenglin.gulimall.search.constant.EsConstant;
import com.lichenglin.gulimall.search.service.ProductSaveService;
import lombok.extern.slf4j.Slf4j;
import org.elasticsearch.action.bulk.BulkItemResponse;
import org.elasticsearch.action.bulk.BulkRequest;
import org.elasticsearch.action.bulk.BulkResponse;
import org.elasticsearch.action.index.IndexRequest;
import org.elasticsearch.client.RestHighLevelClient;
import org.elasticsearch.common.xcontent.XContentType;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

@Slf4j
@Service
public class ProductSaveServiceImpl implements ProductSaveService {

    @Autowired
    RestHighLevelClient restHighLevelClient;
    @Override
    public Boolean productStatusUp(List<SkuEsModel> skuEsModels) throws IOException {
        /**
         * 1.在ES中建立索引,并建立好映射关系
         * 2. 向ES中保存数据
         */
        BulkRequest bulkRequest = new BulkRequest();
        skuEsModels.forEach((item)->{
            //构造保存请求
            IndexRequest indexRequest = new IndexRequest(EsConstant.PRODUCT_INDEX);
            indexRequest.id(item.getSkuId().toString());
            String s = JSON.toJSONString(item);
            indexRequest.source(s, XContentType.JSON);
            bulkRequest.add(indexRequest);
        });

        BulkResponse bulk = restHighLevelClient.bulk(bulkRequest, GuilMallElasticSearchConfig.COMMON_OPTIONS);
       //TODO: 如果批量错误，进行处理
        boolean b = bulk.hasFailures();
        List<Integer> ids = new ArrayList<>();
        for (BulkItemResponse item : bulk.getItems()) {
            ids.add(item.getItemId());
        }
        log.info("商品上架完成：{}",ids);
        return b;
    }
}
