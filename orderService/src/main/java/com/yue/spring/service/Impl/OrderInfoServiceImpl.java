package com.yue.spring.service.Impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.yue.spring.mapper.OrderInfoMapper;
import com.yue.spring.pojo.DO.Order;
import com.yue.spring.pojo.DO.OrderInfo;
import com.yue.spring.service.OrderInfoService;
import org.apache.ibatis.cursor.Cursor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class OrderInfoServiceImpl extends ServiceImpl<OrderInfoMapper,OrderInfo> implements OrderInfoService {

    @Autowired
    private OrderInfoMapper orderInfoMapper;

    @Override
    public Cursor<OrderInfo> selectOrderById(Long id) {
        return orderInfoMapper.selectOrderById(id);
    }

    @Override
    public Order getNearestFlight(Integer userId) {
        return orderInfoMapper.getNearestFlight(userId);
    }
}
