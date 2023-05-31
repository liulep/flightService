package com.yue.spring.client;

import com.yue.spring.pojo.VO.IntegralVo;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.RequestMapping;

@FeignClient("integralservice")
public interface IntegralClient {

    @RequestMapping("integral/getVo")
    public IntegralVo getVo();
}
