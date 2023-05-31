package com.yue.spring.config;

import com.xxl.job.core.handler.annotation.XxlJob;
import com.yue.spring.pojo.constant.RedisConstant;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

@Component
public class HostCityService {

    @Autowired
    private RedisTemplate redisTemplate;


    @XxlJob("refreshDataHour")
    public void refreshDataHour(){
        this.refreshHour();
    }
    @XxlJob("refreshDataWeek")
    public void refreshDataWeek(){
        this.refreshWeek();
    }

    public void refreshHour() {
        // 计算当前的小时key
        long hour = System.currentTimeMillis() / (1000 * 60 * 60);
        List<Integer> hots = RedisConstant.HOT;
        if(hots.size()==0)return;
        for(Integer hot:hots){
            this.redisTemplate.opsForZSet().incrementScore("rank:hour:" + hour, hot, 1);
        }
        RedisConstant.HOT=new ArrayList<>();
    }

    public void refreshWeek(){
        long hour = System.currentTimeMillis() / (1000 * 60 * 60);
        List<String> otherkeys = new ArrayList<>();
        // 2 ：算出近24小时内的key
        for (int i = 0; i < 24 * 7 - 1; i++) {
            String key = "rank:hour:" + (hour - i);
            otherkeys.add(key);
            this.redisTemplate.expire(key,7,TimeUnit.DAYS);
        }
        this.redisTemplate.opsForZSet().unionAndStore("rank:hour:" + hour, otherkeys, "rank:week");
    }

}
