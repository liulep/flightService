package com.yue.spring.exception;

import com.yue.spring.Enum.ResultCodeEnum;
import com.yue.spring.pojo.R;
import com.yue.spring.utils.ResponseUtils;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.stereotype.Component;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

@Component
public class OAuthServerAuthenticationEntryPoint implements AuthenticationEntryPoint {
    @Override
    public void commence(HttpServletRequest request, HttpServletResponse response, AuthenticationException authException) throws IOException {
        ResponseUtils.result(response, R.setResult(ResultCodeEnum.CLIENT_AUTHENTICATION_FAILED));
    }
}
