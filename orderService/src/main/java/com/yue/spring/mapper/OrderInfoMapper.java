package com.yue.spring.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.yue.spring.pojo.DO.Order;
import com.yue.spring.pojo.DO.OrderInfo;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.cursor.Cursor;

@Mapper
public interface OrderInfoMapper extends BaseMapper<OrderInfo> {

    //查询子订单
    @Select("select * from order_info where order_id=#{id}")
    Cursor<OrderInfo> selectOrderById(Long id);

    //查询即将需要出行的订单
    @Select("select o.*  from `order` o\n" +
            "LEFT JOIN order_info i\n" +
            "on o.id=i.order_id\n" +
            "left join route r\n" +
            "on i.line_id = r.id\n" +
            "where o.user_id=#{userId}\n" +
            "and o.STATUS =2\n" +
            "and CONCAT(i.departure_date,' ',r.start_time) > NOW()\n" +
            "order by i.departure_date asc,r.start_time asc\n" +
            "LIMIT 1")
    Order getNearestFlight(@Param("userId") Integer userId);
}
