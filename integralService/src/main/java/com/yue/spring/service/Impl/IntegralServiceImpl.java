package com.yue.spring.service.Impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.yue.spring.mapper.IntegralMapper;
import com.yue.spring.pojo.DO.Integral;
import com.yue.spring.pojo.PO.IntegralPo;
import com.yue.spring.service.IntegralService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class IntegralServiceImpl extends ServiceImpl<IntegralMapper, Integral> implements IntegralService {
    @Autowired
    private IntegralMapper integralMapper;

    @Override
    public IntegralPo getUserIntegral(Integer userId) {
        return integralMapper.getUserIntegral(userId);
    }
}
