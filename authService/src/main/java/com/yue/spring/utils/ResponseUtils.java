package com.yue.spring.utils;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.yue.spring.pojo.R;

import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.nio.charset.StandardCharsets;

/**
 * 结果封装工具类
 */
public class ResponseUtils {
    public static void result(HttpServletResponse response, R r) throws IOException {
        response.setContentType("application/json;charset=UTF-8");
        ServletOutputStream out = response.getOutputStream();
        ObjectMapper objectMapper = new ObjectMapper();
        out.write(objectMapper.writeValueAsString(r).getBytes(StandardCharsets.UTF_8));
        out.flush();
        out.close();
    }
}
