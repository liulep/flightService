package com.yue.spring.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.yue.spring.pojo.City;
import com.yue.spring.pojo.Flight;
import com.yue.spring.pojo.Route;
import com.yue.spring.pojo.VO.FlightNumVo;
import com.yue.spring.pojo.VO.LowPriceRoute;
import com.yue.spring.pojo.VO.RouteEn;
import com.yue.spring.pojo.VO.RouteVo;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;
import java.util.Map;


@Mapper
public interface QueryMapper extends BaseMapper<Route> {

    @Select("select * from city_detail order by city_en asc,abbreviation asc")
    List<City>  getCities();

    @Select("select * from flight_no")
    List<Flight> getFlight();

    @Select("select DISTINCT to_addr_id from route  where from_addr_id=#{id} order by to_addr_id ")
    int []  queryFromCity(Integer id);

    @Select("select DISTINCT from_addr_id from route  where to_addr_id=#{id} order by from_addr_id ")
    int []  queryToCity(Integer id);

    int [] queryByIds(@Param("fromAddrId")String fromAddrId,@Param("toAddrId")String toAddrId,@Param("sendTime")String sendTime);

    IPage<RouteVo> getRouteList(@Param("page") Page<?> page,@Param("param")Map<String,Object> map);

    IPage<RouteVo> getRouteInfo(@Param("page") Page<?> page,@Param("param")Map<String,Object> map);

    List<RouteVo> getHotCitys(@Param("fromId") Integer fromId,@Param("toIds") List<Integer> toIds);
    IPage<RouteVo> getRouteByFlightNum(@Param("page")Page<?> page,@Param("param")FlightNumVo flightNumVo);

    RouteEn getRouteById(Integer id);

    RouteEn getRouteInfoById(@Param("fromAddrId")String fromAddrId,@Param("toAddrId")String toAddrId);

    List<LowPriceRoute> getRouteByLowPrice(@Param("from_addr_id")Integer fromAddrId);

}
