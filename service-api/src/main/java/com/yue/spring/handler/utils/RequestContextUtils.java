package com.yue.spring.handler.utils;

import com.yue.spring.pojo.NonWebRequestAttributes;
import org.springframework.web.context.request.RequestAttributes;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import javax.servlet.http.HttpServletRequest;
import java.util.Arrays;
import java.util.Objects;

public class RequestContextUtils {
    /**
     * 获取HttpServletRequest
     */
    public static HttpServletRequest getRequest(){
        return ((ServletRequestAttributes)(Objects.requireNonNull(RequestContextHolder.getRequestAttributes()))).getRequest();
    }

    public static NonWebRequestAttributes getAttributes(){
        return ((NonWebRequestAttributes) Objects.requireNonNull(RequestContextHolder.getRequestAttributes()));
    }

}
