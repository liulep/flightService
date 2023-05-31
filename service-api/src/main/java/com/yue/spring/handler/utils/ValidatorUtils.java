package com.yue.spring.handler.utils;

import com.yue.spring.Exception.YueException;

import java.util.regex.Pattern;

public class ValidatorUtils {

    //验证邮箱
    private static final String REGEX_EMAIL="^([a-z0-9A-Z]+[-|\\.]?)+[a-z0-9A-Z]@([a-z0-9A-Z]+(-[a-z0-9A-Z]+)?\\.)+[a-zA-Z]{2,}$";

    public static boolean isEmail(String email){
        if(!Pattern.matches(REGEX_EMAIL,email))
            throw new YueException("邮箱格式不正确！");
        return true;
    }

}
