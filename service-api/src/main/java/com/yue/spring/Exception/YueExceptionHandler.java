package com.yue.spring.Exception;

import com.yue.spring.handler.ErrorHandler;
import com.yue.spring.pojo.R;
import lombok.extern.slf4j.Slf4j;
import org.springframework.validation.ObjectError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import javax.servlet.http.HttpServletRequest;
import javax.validation.ConstraintViolation;
import javax.validation.ConstraintViolationException;

/**
 * 全局捕获异常
 */
@RestControllerAdvice("com.yue")
@Slf4j
public class YueExceptionHandler {

    /**
     * 定义自定义全局异常统一捕获
     */
    @ExceptionHandler(YueException.class)
    public ErrorHandler handlerYueException(YueException yue,HttpServletRequest request){
        ErrorHandler errorHandler = ErrorHandler.builder()
                .code(yue.getCode())
                .message(yue.getMessage())
                .build();
        log.error("拦截请求：{}",errorHandler);
        return errorHandler;
    }

    /**
     * JSON异常捕获
     */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ErrorHandler MethodArgumentNotValidExceptionHandler(MethodArgumentNotValidException e) {
        ObjectError objectError = e.getBindingResult().getAllErrors().get(0);
        log.error("拦截请求：{}",objectError);
        return ErrorHandler.builder()
                .code(500)
                .message(objectError.getDefaultMessage())
                .build();
    }


    /**
     * 表单异常捕获
     */
    @ExceptionHandler(ConstraintViolationException.class)
    public ErrorHandler handleValidation(ConstraintViolationException e){
        StringBuffer sb = new StringBuffer();
        for (ConstraintViolation<?> violation : e.getConstraintViolations()) {
            sb.append(violation.getMessage());
        }
        log.error("捕获异常:{}",sb.toString());
        return ErrorHandler.builder()
                .code(500)
                .message(sb.toString())
                .build();
    }

}
