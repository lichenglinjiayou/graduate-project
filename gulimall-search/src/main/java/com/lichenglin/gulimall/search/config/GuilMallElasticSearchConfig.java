package com.lichenglin.gulimall.search.config;

import org.apache.http.HttpHost;
import org.elasticsearch.client.RequestOptions;
import org.elasticsearch.client.RestClient;
import org.elasticsearch.client.RestHighLevelClient;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * 5 整合ES
 *   (1) 导入依赖；
 *   (2) 编写配置；给容器中注入RestHighLevelClient；
 *   (3) 操作ES；
 *
 */
@Configuration
public class GuilMallElasticSearchConfig {
    public static final RequestOptions COMMON_OPTIONS;
    static {
        RequestOptions.Builder builder = RequestOptions.DEFAULT.toBuilder();
        COMMON_OPTIONS = builder.build();
    }

    @Bean
    public RestHighLevelClient getRestHignLevelClient(){
        RestHighLevelClient restHighLevelClient = new RestHighLevelClient(RestClient.builder(new HttpHost("192.168.127.138", 9200, "http")));
        return restHighLevelClient;
    }
}
