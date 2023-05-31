package com.yue.spring.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.yue.spring.pojo.DO.Integral;
import com.yue.spring.pojo.PO.IntegralPo;
import com.yue.spring.pojo.User;
import org.apache.ibatis.annotations.Param;

public interface IntegralService extends IService<Integral> {

    IntegralPo getUserIntegral(Integer userId);
}
