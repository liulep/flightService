package com.yue.spring.config;

import lombok.extern.slf4j.Slf4j;
import org.apache.ibatis.cache.Cache;
import org.springframework.data.redis.core.RedisTemplate;

import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

@Slf4j
public class MybatisCache implements Cache {

    private static RedisTemplate<String,Object> redisTemplate;
    private final ReadWriteLock readWriteLock = new ReentrantReadWriteLock();

    private final static Integer EXP_TIME=30; //秒

    private final String id;

    public MybatisCache(String id){
        if(redisTemplate==null){
            redisTemplate= ApplicationContextHolder.getBean("redisTemplate");
        }
        this.id=id;
    }

    @Override
    public String getId() {
        return this.id;
    }

    @Override
    public void putObject(Object key, Object value) {
            redisTemplate.opsForHash().put(id, key.toString() , value);
    }

    @Override
    public Object getObject(Object key) {
            return redisTemplate.opsForHash().get(id , key.toString());
    }

    @Override
    public Object removeObject(Object key) {
        return redisTemplate.delete(key.toString());
    }

    @Override
    public void clear() {
        redisTemplate.delete(id.toString());
    }

    @Override
    public int getSize() {
        return redisTemplate.opsForHash().size(id.toString()).intValue();
    }

    @Override
    public ReadWriteLock getReadWriteLock() {
        return  readWriteLock;
    }
}
