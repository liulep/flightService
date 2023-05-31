package com.yue.spring.handler.utils;

import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;

import java.sql.Time;
import java.util.concurrent.TimeUnit;

public class RedissonLockUtil {
    private static RedissonClient redissonClient;

    public void setRedissonClient(RedissonClient locker){
        redissonClient=locker;
    }

    /**
     * 上锁
     * @param key
     * @return
     */
    public static RLock lock(String key){
        RLock lock = redissonClient.getLock(key);
        lock.lock();
        return lock;
    }

    public static RLock lock(String key, TimeUnit unit,int timeout){
        RLock lock=redissonClient.getLock(key);
        lock.lock(timeout,unit);
        return lock;
    }

    public static RLock lock(String key, int timeout){
        RLock lock=redissonClient.getLock(key);
        lock.lock(timeout,TimeUnit.SECONDS);
        return lock;
    }

    /**
     * 获取锁
     * @param key
     * @param waitTime
     * @param leaseTime
     * @return
     */
    public static Boolean tryLock(String key,int waitTime,int leaseTime){
        RLock lock=redissonClient.getLock(key);
        try {
            return lock.tryLock(waitTime,leaseTime,TimeUnit.SECONDS);
        } catch (InterruptedException e) {
            return false;
        }
    }

    public static Boolean tryLock(String key,TimeUnit unit,int waitTime,int leaseTime){
        RLock lock=redissonClient.getLock(key);
        try {
            return lock.tryLock(waitTime,leaseTime,unit);
        } catch (InterruptedException e) {
            return false;
        }
    }

    /**
     * 释放锁
     * @param key
     */
    public static void unlock(String key){
        RLock lock = redissonClient.getLock(key);
        lock.unlock();
    }

    public static void unlock(RLock lock){
        lock.unlock();
    }

}
