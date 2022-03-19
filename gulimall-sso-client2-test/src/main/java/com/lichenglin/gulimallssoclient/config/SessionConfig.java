package com.lichenglin.gulimallssoclient.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.serializer.GenericJackson2JsonRedisSerializer;
import org.springframework.data.redis.serializer.RedisSerializer;
import org.springframework.session.web.http.CookieSerializer;
import org.springframework.session.web.http.DefaultCookieSerializer;
@Configuration
public class SessionConfig {

    @Bean
    public CookieSerializer cookieSerializer(){
        DefaultCookieSerializer cookieSerializer = new DefaultCookieSerializer();
        cookieSerializer.setCookieName("SOO_SESSION");
        return cookieSerializer;
    }

    @Bean
    public RedisSerializer<Object> redisSerializer(){
        return new GenericJackson2JsonRedisSerializer();
    }
}
