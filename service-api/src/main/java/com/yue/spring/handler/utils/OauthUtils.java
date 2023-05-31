package com.yue.spring.handler.utils;

import com.yue.spring.pojo.constant.LoginVal;

/**
 * OAuth2.0工具类，从请求的线程中获取个人信息
 */
public class OauthUtils {

    /**
     * 获取当前请求登录用户的详细信息
     */
    public static LoginVal getCurrentUser(){
        return (LoginVal) RequestContextUtils.getRequest().getAttribute("loginVal_attribute");
    }
}
