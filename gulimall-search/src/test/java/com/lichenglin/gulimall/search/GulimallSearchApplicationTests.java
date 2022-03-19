package com.lichenglin.gulimall.search;

import com.alibaba.fastjson.JSON;
import com.lichenglin.gulimall.search.config.GuilMallElasticSearchConfig;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;
import org.elasticsearch.action.index.IndexRequest;
import org.elasticsearch.action.index.IndexResponse;
import org.elasticsearch.action.search.SearchRequest;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.client.RestHighLevelClient;
import org.elasticsearch.common.xcontent.XContentType;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.search.SearchHit;
import org.elasticsearch.search.SearchHits;
import org.elasticsearch.search.aggregations.Aggregation;
import org.elasticsearch.search.aggregations.AggregationBuilders;
import org.elasticsearch.search.aggregations.Aggregations;
import org.elasticsearch.search.aggregations.bucket.terms.Terms;
import org.elasticsearch.search.aggregations.metrics.Avg;
import org.elasticsearch.search.builder.SearchSourceBuilder;
import org.junit.jupiter.api.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import javax.xml.ws.soap.Addressing;
import java.io.IOException;
import java.util.List;

@SpringBootTest
@RunWith(SpringJUnit4ClassRunner.class)
public class GulimallSearchApplicationTests {

    @Autowired
    RestHighLevelClient restHighLevelClient;



    @Data
    @ToString
    static class Account {
        private int account_number;
        private int balance;
        private String firstname;
        private String lastname;
        private int age;
        private String gender;
        private String address;
        private String employer;
        private String email;
        private String city;
        private String state;
    }
    @Test
    public void contextLoads() throws IOException {
//        System.out.println(restHighLevelClient);
        IndexRequest user = new IndexRequest("user");
        user.id("1");
//        user.source("username","zhangsan");
        User user1 = new User("zhangsan", "male", 22);
        String s = JSON.toJSONString(user1);
        user.source(s, XContentType.JSON);

        IndexResponse index = restHighLevelClient.index(user, GuilMallElasticSearchConfig.COMMON_OPTIONS);
        System.out.println(index);
    }

    @Test
    public void searchData() throws IOException {
        SearchRequest searchRequest = new SearchRequest();
        searchRequest.indices("bank");
        SearchSourceBuilder searchSourceBuilder = new SearchSourceBuilder();
        searchSourceBuilder.query(QueryBuilders.matchQuery("address","mill"));
        searchSourceBuilder.aggregation(AggregationBuilders.terms("age_agg").field("age").size(10));
        searchSourceBuilder.aggregation(AggregationBuilders.avg("balance").field("balance"));
//        searchSourceBuilder.from();
//        searchSourceBuilder.size();
        searchRequest.source(searchSourceBuilder);
        SearchResponse search = restHighLevelClient.search(searchRequest, GuilMallElasticSearchConfig.COMMON_OPTIONS);


        //6 获取所有命中的记录
        SearchHits hits = search.getHits();
        SearchHit[] hits1 = hits.getHits();
        for (SearchHit documentFields : hits1) {
            String sourceAsString = documentFields.getSourceAsString();
            Account account = JSON.parseObject(sourceAsString, Account.class);
            System.out.println(account);
        }
        //6 获取聚合后的信息：平均薪资等......
        Aggregations aggregations = search.getAggregations();
        Terms aggregation = aggregations.get("age_agg");
        aggregation.getBuckets().forEach((item)->{
            System.out.println("年龄："+item.getKeyAsString());
        });
        Avg aggregation1 = aggregations.get("balance");
        System.out.println("平均薪资："+aggregation1.getValue());
    }
    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    class User{
        private String username;
        private String gender;
        private Integer age;
    }
}
