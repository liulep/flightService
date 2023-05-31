package com.yue.spring.service.Impl;

import cn.hutool.core.date.DateTime;
import cn.hutool.core.date.DateUtil;
import com.yue.spring.pojo.Stock;
import com.yue.spring.pojo.constant.RedisConstant;
import com.yue.spring.service.StockService;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataAccessException;
import org.springframework.data.redis.connection.RedisConnection;
import org.springframework.data.redis.core.RedisCallback;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import javax.annotation.PostConstruct;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;

@Service
@SuppressWarnings("all")
@Slf4j
public class IntiService {

    @Autowired
    private StockService stockService;

    @Autowired
    private RedisTemplate redisTemplate;

    private final static String INIT_STOCK="line_stock:";

    //重置库存30天
    @PostConstruct
    public void init() throws ParseException {
//        //初始化库存放入redis
          SimpleDateFormat format=new SimpleDateFormat("yyyy-MM-dd");
//        String today = DateUtil.today();
//        Calendar calendar=Calendar.getInstance();
//        calendar.setTime(format.parse(today));
//        DateTime dateTime = DateUtil.offsetMonth(new Date(), 1);
//        Date parse = format.parse(String.valueOf(dateTime));
//        while(calendar.getTime().compareTo(parse)<=0){
//            int sendTime = DateUtil.dayOfWeek(calendar.getTime());
//            sendTime=sendTime-1;
//            if(sendTime<0)sendTime=6;
//            List<Stock> stocks = stockService.getStockByWeek(sendTime);
//            log.error("{}",stocks);
//            stocks.forEach(stock -> {
//                log.error("{}",INIT_STOCK+format.format(calendar.getTime())+stock.getLineId());
//                redisTemplate.opsForValue().set(INIT_STOCK+format.format(calendar.getTime())+":"+stock.getLineId(),stock.getStock());
//            });
//           calendar.add(Calendar.DATE,1);
//        }
//        list.forEach(stock -> {
//            redisTemplate.opsForValue().set(INIT_STOCK+stock.getLineId(),stock.getStock());
//        });
//        Calendar calendar=Calendar.getInstance();
//        calendar.setTime(format.parse("2022-07-23"));
//        int sendTime = DateUtil.dayOfWeek(calendar.getTime());
//            sendTime=sendTime-1;
//            if(sendTime<0)sendTime=6;
//        List<Stock> stocks = stockService.getStockByWeek(sendTime);
//        log.error("{}",stocks);
//        stocks.forEach(stock -> {
//             log.error("{}",INIT_STOCK+format.format(calendar.getTime())+stock.getLineId());
//             redisTemplate.opsForValue().set(INIT_STOCK+format.format(calendar.getTime())+":"+stock.getLineId(),stock.getStock());
//        });
//        log.error("初始化成功！");

        Map<String,Object> map=new HashMap<>();
        map.put("sendTime","2022-06-28");
        int sendTime = DateUtil.dayOfWeek(format.parse((String)map.get("sendTime")));
        sendTime=sendTime-1;
        if(sendTime<0)sendTime=6;
        map.put("sendTimeOne",sendTime);
        if(sendTime==6)map.put("sendTimeTwo",0);
        else map.put("sendTimeTwo",sendTime+1);
        log.error("sendTimeOne -> {}, sendTimeTwo -> {}",map.get("sendTimeOne"),map.get("sendTimeTwo"));
    }


    //定时任务每天晚上0点
    @Scheduled(cron = "0 0 0 * * ?")
    private void getStockDay() throws ParseException {
        SimpleDateFormat format=new SimpleDateFormat("yyyy-MM-dd");
        DateTime dateTime = DateUtil.offsetMonth(new Date(), 1);
        Date parse = format.parse(String.valueOf(dateTime));
        int sendTime = DateUtil.dayOfWeek(parse);
        sendTime=sendTime-1;
        if(sendTime<0)sendTime=6;
        List<Stock> stockByWeek = stockService.getStockByWeek(sendTime);
        stockByWeek.forEach(stock -> {
            redisTemplate.opsForValue().set(INIT_STOCK+format.format(parse)+":"+stock.getLineId(),stock.getStock());
        });
        DateTime yesterday = DateUtil.yesterday();
        Date yer = format.parse(String.valueOf(yesterday));
        redisTemplate.delete(INIT_STOCK+format.format(yer)+":*");
        log.info("座位数初始化成功！");
    }


}
