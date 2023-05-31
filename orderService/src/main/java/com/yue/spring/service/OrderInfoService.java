package com.yue.spring.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.yue.spring.pojo.DO.Order;
import com.yue.spring.pojo.DO.OrderInfo;
import org.apache.ibatis.cursor.Cursor;

public interface OrderInfoService extends IService<OrderInfo> {

    //查询子订单
    Cursor<OrderInfo> selectOrderById(Long id);

    //查询即将需要出行的订单
    Order getNearestFlight(Integer userId);
}
