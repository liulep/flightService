package com.yue.spring.handler;

import com.alibaba.fastjson2.JSON;
import com.yue.spring.group.NotControllerResponseAdvice;
import com.yue.spring.pojo.R;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.core.MethodParameter;
import org.springframework.http.MediaType;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.http.server.ServerHttpRequest;
import org.springframework.http.server.ServerHttpResponse;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.servlet.mvc.method.annotation.ResponseBodyAdvice;

/**
 * 定义全局结果返回
 */
@ControllerAdvice(basePackages = "com.yue")
@ConditionalOnClass(ResponseBodyAdvice.class)
public class ResultResponseHandler implements ResponseBodyAdvice<Object> {
    @Override
    public boolean supports(MethodParameter returnType, Class<? extends HttpMessageConverter<?>> converterType) {
        return !returnType.hasMethodAnnotation(NotControllerResponseAdvice.class);
    }

    @Override
    public Object beforeBodyWrite(Object body, MethodParameter returnType, MediaType selectedContentType, Class<? extends HttpMessageConverter<?>> selectedConverterType, ServerHttpRequest request, ServerHttpResponse response) {
        if(body instanceof String){
            return JSON.toJSONString(R.ok(body));
        }
        if(body instanceof ErrorHandler){
            ErrorHandler error=(ErrorHandler) body;
            return R.error().message(error.getMessage()).code(error.getCode());
        }
        return R.ok(body);
    }
}
