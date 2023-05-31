package com.yue.spring.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.yue.spring.pojo.DO.Order;
import com.yue.spring.pojo.DO.OrderInfo;
import com.yue.spring.pojo.DTO.OrderDTO;
import com.yue.spring.pojo.PlaneOrder;
import org.apache.ibatis.cursor.Cursor;

public interface OrderService extends IService<Order> {
    void returnStock(OrderDTO order);

    void useCoupon(Integer id);

}
