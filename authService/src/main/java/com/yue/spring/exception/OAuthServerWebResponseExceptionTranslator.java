package com.yue.spring.exception;

import com.yue.spring.Enum.ResultCodeEnum;
import com.yue.spring.pojo.R;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.oauth2.common.exceptions.InvalidGrantException;
import org.springframework.security.oauth2.common.exceptions.UnsupportedGrantTypeException;
import org.springframework.security.oauth2.provider.error.WebResponseExceptionTranslator;


@SuppressWarnings("ALL")
public class OAuthServerWebResponseExceptionTranslator implements WebResponseExceptionTranslator{
    /**
     * 业务处理方法，重写这个方法返回客户端信息
     */
    @Override
    public ResponseEntity<R> translate(Exception e){
        R r = doTranslateHandler(e);
        return new ResponseEntity<>(r, HttpStatus.UNAUTHORIZED);
    }

    private R doTranslateHandler(Exception e) {
        //初始值，系统错误，
        ResultCodeEnum code=ResultCodeEnum.UNAUTHORIZED;
        //判断异常，不支持的认证方式
        if(e instanceof UnsupportedGrantTypeException){
            code=ResultCodeEnum.UNSUPPORTED_GRANT_TYPE;
            //用户名或密码异常
        }else if(e instanceof InvalidGrantException){
            code=ResultCodeEnum.USERNAME_OR_PASSWORD_ERROR;
        }
        return R.setResult(code);
    }
}
