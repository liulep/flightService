package com.yue.gray.utils;

import java.util.Objects;


public class GrayRequestContextHolder {

    //保存灰度标记
    public static ThreadLocal<String> grayTag=new ThreadLocal<>();

    public static ThreadLocal<String> getGrayTag(){
        String tag = grayTag.get();
        if (Objects.isNull(tag))
            grayTag.set(null);
        return grayTag;
    }

    public static void setGrayTag(String tag){
        grayTag.set(tag);
    }

    public static void remove(){
        grayTag.remove();
    }

//    public static ThreadLocal<Map<String,String>> currentContext=new ThreadLocal<>();
//
//    public static Map<String,String> getCurrentContext(){
//        Map<String, String> map = currentContext.get();
//        if (Objects.isNull(map)){
//            map=new HashMap<>();
//        }
//        return map;
//    }
//
//    public static void setCurrentContext(Map<String,String> map){
//        currentContext.set(map);
//    }
//
//    public static void put(String key,String value){
//        HashMap<String, String> map = new HashMap<>();
//        map.put(key,value);
//        currentContext.set(map);
//    }
//
//
//    public static void clearContext(){
//        currentContext.remove();
//    }

}

