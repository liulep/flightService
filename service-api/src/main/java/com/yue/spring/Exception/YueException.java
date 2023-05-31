package com.yue.spring.Exception;

import com.yue.spring.Enum.ResultCodeEnum;
import lombok.Data;
import lombok.EqualsAndHashCode;

@EqualsAndHashCode(callSuper = true)
@Data
public class YueException extends RuntimeException{
    private Integer code;
    private String message;

    public YueException(ResultCodeEnum resultCodeEnum){
        this.code=resultCodeEnum.getCode();
        this.message=resultCodeEnum.getMessage();
    }

    public YueException(Integer code,String message){
        this.code=code;
        this.message=message;
    }

    public YueException(String message){
        this.code=500;
        this.message=message;
    }
}
