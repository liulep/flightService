package com.yue.spring.service.Impl;

import cn.hutool.core.date.DateTime;
import cn.hutool.core.date.DateUtil;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.yue.spring.mapper.QueryMapper;
import com.yue.spring.pojo.City;
import com.yue.spring.pojo.Flight;
import com.yue.spring.pojo.Route;
import com.yue.spring.pojo.VO.FlightNumVo;
import com.yue.spring.pojo.VO.LowPriceRoute;
import com.yue.spring.pojo.VO.RouteEn;
import com.yue.spring.pojo.VO.RouteVo;
import com.yue.spring.service.QueryService;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;

@Service
@Slf4j
public class QueryServiceImpl extends ServiceImpl<QueryMapper, Route> implements QueryService {

    @Autowired
    private QueryMapper queryMapper;

    @Autowired
    private RedisTemplate redisTemplate;

    @Override
    public List<City> getCities() {
        return queryMapper.getCities();
    }

    @Override
    public List<Flight> getFlight() {
        return queryMapper.getFlight();
    }

    @Override
    public IPage<RouteVo> getRouteInfo(Map<String, Object> map, Integer currentPage, Integer pageSize) throws ParseException {
        Page<RouteVo> page = new Page<>(currentPage, pageSize);
        SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd");
        String backTime=(String)map.get("backTime");
        if(StringUtils.isNotBlank(backTime)||String.valueOf(map.get("sendTime")).equals(format.format(new Date()))){
            map.put("now",StringUtils.isNotBlank(backTime)?backTime:new SimpleDateFormat("HH:mm:ss").format(new Date()));
        }
        int sendTime = DateUtil.dayOfWeek(format.parse((String)map.get("sendTime")));
        sendTime=sendTime-1;
        if(sendTime<0)sendTime=6;
        map.put("sendTimeOne",sendTime);
        if(sendTime==6)map.put("sendTimeTwo",0);
        else map.put("sendTimeTwo",sendTime+1);
        if(Integer.parseInt((String)map.get("type"))==0){ // 0 -> 直飞
            return directFlight(map,page);
        }
        else{  // 1  -> 中转
            return connectingFlight(map,page);
        }
    }

    @Override
    public List<RouteVo> getHotCitys(Integer fromId, List<Integer> toIds) {
        return queryMapper.getHotCitys(fromId,toIds);
    }

    @Override
    public IPage<RouteVo> getRouteByFlightNum(FlightNumVo flightNumVo, Integer currentPage, Integer pageSize) throws ParseException {
        IPage<RouteVo> routeByFlightNum = queryMapper.getRouteByFlightNum(new Page<>(currentPage, pageSize), flightNumVo);
        Map<String,Object> map=new HashMap<String,Object>(){{
            put("sendTime",new SimpleDateFormat("yyyy-MM-dd").format(flightNumVo.getSendTime()));
        }};
        flight(routeByFlightNum, map);
        return routeByFlightNum;
    }

    @Override
    public RouteEn getRouteById(Integer id) throws ParseException {
        RouteEn routeById = queryMapper.getRouteById(id);
        SimpleDateFormat format = new SimpleDateFormat("HH:mm:ss");
        long l = format.parse(routeById.getEndTime()).getTime() - format.parse(routeById.getStartTime()).getTime();
        Integer second=(int)l/1000;
        routeById.setHour(second);
        return routeById;
    }

    @Override
    public RouteEn getRouteInfoById(String fromAddrId, String toAddrId) throws ParseException {
        RouteEn routeById = queryMapper.getRouteInfoById(fromAddrId, toAddrId);
        SimpleDateFormat format = new SimpleDateFormat("HH:mm:ss");
        if (routeById.getNextStartTime()!=null&&routeById.getNextEndTime()!=null){
            routeById.setNextHour(compareTo(format.parse(routeById.getNextStartTime()), format.parse(routeById.getNextEndTime())));
            //两趟航班的相差时间
            routeById.setMiddleHour(compareTo(format.parse(routeById.getStartTime()),format.parse(routeById.getNextEndTime())));
            //价格
            routeById.setTotalPrice(routeById.getPrice() + routeById.getNextPrice());
        }
        routeById.setHour(compareTo(format.parse(routeById.getStartTime()), format.parse(routeById.getEndTime())));
        return routeById;
    }

    @Override
    public List<LowPriceRoute> lowPrice(Integer id) {
        List<LowPriceRoute> routeByLowPrice = queryMapper.getRouteByLowPrice(id);
        SimpleDateFormat format=new SimpleDateFormat("yyyy-MM-dd");
        try {
            int sendTime = DateUtil.dayOfWeek(new DateTime());
            sendTime=sendTime-1;
            if(sendTime<0)sendTime=6;
            for(LowPriceRoute next:routeByLowPrice){
                int week=next.getSendTime();
                int t=0;
                if(week>sendTime) t=week-sendTime;
                else if(week<sendTime) t=7-sendTime+week;
                Calendar calendar=Calendar.getInstance();
                calendar.setTime(new DateTime());
                calendar.add(Calendar.DATE,t);
                next.setDateTime(format.format(calendar.getTime()));
            }
        }catch (Exception e){
            e.printStackTrace();
        }
        return routeByLowPrice;
    }

    public IPage<RouteVo> directFlight(Map<String,Object> map,Page<RouteVo> page) throws ParseException {
        IPage<RouteVo> routeInfo = queryMapper.getRouteInfo(page, map);
        flight(routeInfo,map);
        return routeInfo;
    }

    public IPage<RouteVo> connectingFlight(Map<String,Object>map, Page<RouteVo> page) throws ParseException {
        IPage<RouteVo> routeList = queryMapper.getRouteList(page, map);
        flight(routeList,map);
        return routeList;
    }

    public int compareTo(Date v1,Date v2){
        int i=v1.compareTo(v2);
        long j=0;
        if(i < 0)
            j=(v2.getTime()-v1.getTime())/60000;
        else if(i==0)
            j=720;
        else {
            j=((86400000-v1.getTime())+v2.getTime())/60000;
        }
        return (int) j;
    }

    @Async
    public void flight(IPage<RouteVo> routeList,Map<String,Object>map) throws ParseException {
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("HH:mm:ss");
        for(Iterator<RouteVo> item = routeList.getRecords().iterator(); item.hasNext();){
            RouteVo routeVo=item.next();
            if(routeVo.getToAddrId().equals(routeVo.getNextToAddrId())){
                routeVo.setNextToAddrId(null);
                routeVo.setNextToCity(null);
                routeVo.setNextId(null);
            }
            if (routeVo.getNextStartTime()!=null&&routeVo.getNextEndTime()!=null){
                if(routeVo.getStartTime().compareTo(routeVo.getEndTime())>=1&&routeVo.getSendTime().equals(routeVo.getNextSendTime()) ||
                        routeVo.getFlightNum().equals(routeVo.getNextFlightNum())){
                    item.remove();
                    continue;
                }
                routeVo.setNextHour(compareTo(routeVo.getNextStartTime(), routeVo.getNextEndTime()));
                //两趟航班的相差时间
                routeVo.setMiddleHour(compareTo(routeVo.getStartTime(),routeVo.getNextEndTime()));
                //价格
                routeVo.setTotalPrice(routeVo.getPrice() + routeVo.getNextPrice());
                //日期
                if(routeVo.getSendTime()<routeVo.getNextSendTime()){
                    SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd");
                    Calendar calendar=Calendar.getInstance();
                    calendar.setTime(format.parse((String)map.get("sendTime")));
                    calendar.add(Calendar.DATE,1);
                    routeVo.setNextDateTime(format.format(calendar.getTime()));
                }
                else routeVo.setNextDateTime((String)map.get("sendTime"));
            }
            routeVo.setHour(compareTo(routeVo.getStartTime(), routeVo.getEndTime()));
            routeVo.setDateTime((String) map.get("sendTime"));
        }
    }

}
