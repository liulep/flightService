package com.yue.spring.config;

import io.lettuce.core.dynamic.annotation.CommandNaming;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.support.atomic.RedisAtomicLong;
import org.springframework.stereotype.Component;

import java.util.concurrent.TimeUnit;

/**
 * 自动生成订单号自增
 */
@Component
@SuppressWarnings("all")
public class CacheService {

    @Autowired
    private RedisTemplate redisTemplate;

    public Long getIncrementNum(String key){
        RedisAtomicLong entityIdCounter = new RedisAtomicLong(key, redisTemplate.getConnectionFactory());
        Long l = entityIdCounter.incrementAndGet();
        if(l==null || l.longValue()==1){
            entityIdCounter.expire(1, TimeUnit.DAYS);
        }
        return l;
    }

}
