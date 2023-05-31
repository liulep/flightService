package com.yue.spring.utils;

import com.yue.spring.config.CacheService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * 订单号获取
 */
@Component
public class SequenceUtils {

    @Autowired
    private CacheService cacheService;

    private final static int DEFAULT_LENGTH=5;

    public  String getSequence(){
        String currentDate = new SimpleDateFormat("yyyyMMdd").format(new Date());
        Long num = cacheService.getIncrementNum("id:generator:order:" + currentDate);
        String str = String.valueOf(num);
        int len = str.length();
        StringBuilder sb = new StringBuilder();
        sb.append(currentDate);
        if (len >= DEFAULT_LENGTH) {
            sb.append(str);
            return sb.toString();
        }
        int rest = DEFAULT_LENGTH - len;
        for (int i = 0; i < rest; i++) {
            sb.append('0');
        }
        sb.append(str);
        return sb.toString();
    }
}
