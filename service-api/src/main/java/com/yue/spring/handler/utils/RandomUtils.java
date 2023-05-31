package com.yue.spring.handler.utils;

import java.util.Random;

public class RandomUtils {
    public static String getRandom(){
        return String.valueOf((int)(Math.random()*9000)+1000);
    }
}
