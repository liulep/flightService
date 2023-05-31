package com.yue.spring.service.Impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.yue.spring.client.CouponClient;
import com.yue.spring.handler.utils.OauthUtils;
import com.yue.spring.mapper.OrderMapper;
import com.yue.spring.pojo.DO.Order;
import com.yue.spring.pojo.DO.OrderInfo;
import com.yue.spring.pojo.DTO.OrderDTO;
import com.yue.spring.pojo.PlaneOrder;
import com.yue.spring.service.OrderService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.text.SimpleDateFormat;

@Service
public class OrderServiceImpl extends ServiceImpl<OrderMapper, Order> implements OrderService {

    private final static String INIT_STOCK = "line_stock:";

    private final static String ORDER_INFO = "order_num_user:";

    private final static String ORDER_USER = "order_user_list:";

    @Autowired
    private RedisTemplate redisTemplate;

    @Autowired
    private CouponClient couponClient;

    @Override
    @Async
    public void returnStock(OrderDTO order) {
        SimpleDateFormat format=new SimpleDateFormat("yyyy-MM-dd");
        int length = order.getUserRelationIds().split(",").length;
        //删除用户订单信息
        redisTemplate.delete(ORDER_USER+ OauthUtils.getCurrentUser().getUserId());
        //删除订单残留信息
        redisTemplate.delete(ORDER_INFO+order.getId());
        //返回库存
        if(order.getStatus()!=2){
            String[] date = new String[]{format.format(order.getFromTime()), order.getToTime() == null ? null : format.format(order.getToTime())};
            int i=0;
            for(String lineId:order.getLineIds().split(",")){
                String s=date[i];
                if (s==null) s=date[0];
                //归还库存
                redisTemplate.opsForValue().increment(INIT_STOCK +s+":"+ lineId,length);
                i++;
            }
        }
    }

    @Override
    @Async
    public void useCoupon(Integer id) {
        couponClient.use(id);
    }
}
