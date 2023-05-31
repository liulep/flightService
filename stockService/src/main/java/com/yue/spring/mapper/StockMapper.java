package com.yue.spring.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.yue.spring.pojo.Stock;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

@Mapper
public interface StockMapper extends BaseMapper<Stock> {

    @Select("select s.* from stock s left join route r on r.id=s.line_id where r.send_time=#{sendWeek}")
    List<Stock> getStockByWeek(@Param("sendWeek") int sendWeek);
}
