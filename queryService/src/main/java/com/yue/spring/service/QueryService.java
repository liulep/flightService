package com.yue.spring.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.IService;
import com.yue.spring.pojo.City;
import com.yue.spring.pojo.Flight;
import com.yue.spring.pojo.Route;
import com.yue.spring.pojo.VO.FlightNumVo;
import com.yue.spring.pojo.VO.LowPriceRoute;
import com.yue.spring.pojo.VO.RouteEn;
import com.yue.spring.pojo.VO.RouteVo;
import org.apache.ibatis.annotations.Param;

import java.text.ParseException;
import java.util.List;
import java.util.Map;

public interface QueryService extends IService<Route> {

    List<City> getCities();

    List<Flight> getFlight();


    IPage<RouteVo> getRouteInfo(Map<String,Object> map, Integer currentPage, Integer pageSize) throws ParseException;

    List<RouteVo> getHotCitys(Integer fromId,List<Integer> toIds);

    IPage<RouteVo> getRouteByFlightNum(FlightNumVo flightNumVo,Integer currentPage,Integer pageSize) throws ParseException;

    RouteEn getRouteById(Integer id) throws ParseException;

    RouteEn getRouteInfoById(String fromAddrId, String toAddrId) throws ParseException;

    List<LowPriceRoute> lowPrice(Integer id);
}
