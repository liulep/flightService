package com.yue.spring.interceptor;

import cn.hutool.core.util.StrUtil;
import com.yue.gray.rule.GrayConstant;
import com.yue.gray.utils.GrayRequestContextHolder;
import com.yue.spring.handler.utils.RequestContextUtils;
import com.yue.spring.pojo.NonWebRequestAttributes;
import com.yue.spring.pojo.constant.TokenConstant;
import feign.RequestInterceptor;
import feign.RequestTemplate;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestAttributes;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import javax.servlet.http.HttpServletRequest;
import java.util.Enumeration;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * feign携带token信息
 */
@Component
public class FeignRequestInterceptor implements RequestInterceptor {



    @Override
    public void apply(RequestTemplate template) {
        RequestAttributes requestAttributes = RequestContextHolder.getRequestAttributes();
        if(requestAttributes instanceof ServletRequestAttributes){
            //从RequestContextHolder中获取HttpServletRequest
            HttpServletRequest httpServletRequest = RequestContextUtils.getRequest();
            //获取RequestContextHolder中的信息
            Map<String, String> headers = getHeaders(httpServletRequest);
            //放入feign的RequestTemplate中
            for (Map.Entry<String, String> entry : headers.entrySet()) {
                template.header(entry.getKey(), entry.getValue());
            }
        }
        else if(requestAttributes instanceof NonWebRequestAttributes){
            NonWebRequestAttributes attributes = RequestContextUtils.getAttributes();
            template.header("Authorization",(String)attributes.getAttribute("Authorization",0));
            template.header("jwt-token",(String)attributes.getAttribute("jwt-token",0));
        }

    }

    /**
     * 获取原请求头
     */
    private Map<String, String> getHeaders(HttpServletRequest request) {
        Map<String, String> map = new LinkedHashMap<>();
        Enumeration<String> enumeration = request.getHeaderNames();
        if (enumeration != null) {
            while (enumeration.hasMoreElements()) {
                String key = enumeration.nextElement();
                String value = request.getHeader(key);
                if (StrUtil.equals(TokenConstant.TOKEN_NAME,key)){
                    map.put(key, value);
                }

                if (StrUtil.equals(GrayConstant.GRAY_HEADER,key)){
                    map.put(key, value);
                }
            }
        }
        return map;
    }
}
