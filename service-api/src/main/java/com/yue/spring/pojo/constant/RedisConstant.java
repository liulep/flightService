package com.yue.spring.pojo.constant;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

public class RedisConstant {

    public static Map<Long, Map<Integer,Integer>> PV=new ConcurrentHashMap<>();

    public final static String CACHE_ONE_PV_LIST="cache_one_pv_list:";

    public final static String CACHE_SECOND_PV_LIST="cache_two_pv_list:";

    public static List<Integer> HOT=new ArrayList<>();

    public static Map<String,Boolean> StockMap=new HashMap<>();
}
