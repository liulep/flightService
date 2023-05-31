package com.yue.spring.controller;

import cn.hutool.core.date.DateUtil;
import cn.hutool.http.HttpUtil;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.yue.spring.Exception.YueException;
import com.yue.spring.group.NotControllerResponseAdvice;
import com.yue.spring.pojo.City;
import com.yue.spring.pojo.Flight;
import com.yue.spring.pojo.constant.RedisConstant;
import com.yue.spring.pojo.VO.FlightNumVo;
import com.yue.spring.pojo.VO.LowPriceRoute;
import com.yue.spring.pojo.VO.RouteEn;
import com.yue.spring.pojo.VO.RouteVo;
import com.yue.spring.service.CityService;
import com.yue.spring.service.QueryService;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.ZSetOperations;
import org.springframework.web.bind.annotation.*;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;

@RestController
@RequestMapping("query")
@Slf4j
public class QueryController implements ApplicationContextAware {

    @Autowired
    private QueryService queryService;

    @Autowired
    private CityService cityService;

    @Autowired
    private StringRedisTemplate redisTemplate;

    private ApplicationContext context;

    private final static String ALL_CITY="city_all:";
    private final static String ALL_FLIGHT="flight_all:";

    private final static String To_CITY="to_city:";
    private final static String FROM_CITY="from_city:";

    private final static String TIME="-time";

    private final static String PRICE="-price";

    private final static String HOT="hot:";


    //查询所有城市
    @Cacheable(ALL_CITY)
    @GetMapping("/city/all")
    public Object getCities(){
        List<City> cities = queryService.getCities();
        Map<String, Map<String,List<City>>> maps=new HashMap<>();
        Map<String,List<City>> map=new HashMap<>();
        for(int i=0;i<cities.size();){
            String cityName=cities.get(i).getCityEn().substring(0,1);
            List<City> cityList=new ArrayList<>();
            while(i<cities.size()&&cities.get(i).getCityEn().substring(0,1).toUpperCase().equals(cityName)){
                cityList.add(cities.get(i));
                i++;
            }
            map.put(cityName,cityList);
            if(cityName.charAt(0)=='F'){
                maps.put("ABCDEF",map);
                map=new HashMap<>();
            }
            else if(cityName.charAt(0)=='J'){
                maps.put("GHIJ",map);
                map=new HashMap<>();
            }
            else if(cityName.charAt(0)=='N'){
                maps.put("KLMN",map);
                map=new HashMap<>();
            }
            else if(cityName.charAt(0)=='W'){
                maps.put("PQRSTUVW",map);
                map=new HashMap<>();
            }
            else if(cityName.charAt(0)=='Z'){
                maps.put("XYZ",map);
                map=new HashMap<>();
            }
        }
        return maps;
    }


    //查询所有航班
    @Cacheable(ALL_FLIGHT)
    @GetMapping("/all/flight")
    public List<Flight> allFlight(){
        return queryService.getFlight();
    }


    //航班查询
    @GetMapping("/flight/search")
    public IPage<RouteVo> search(@RequestParam Map<String,Object> map,
                       @RequestParam(value = "currentPage",defaultValue = "1")Integer currentPage,
                       @RequestParam(value = "pageSize",defaultValue = "10")Integer pageSize,
                       @RequestParam(value = "sort",defaultValue = "0")Integer sort) throws ParseException {
        map.put("sort",sort);
        if(map.containsKey("endPrice")&&StringUtils.isBlank((String)map.get("endPrice")))
            map.remove("endPrice");
        if(map.containsKey("startPrice")&&StringUtils.isBlank((String)map.get("startPrice")))
            map.remove("startPrice");
        if(map.containsKey("backTime")&&StringUtils.isBlank((String)map.get("backTime")))
            map.remove("backTime");
        if(map.containsKey("time")&&StringUtils.isNotBlank((String)map.get("time"))){
            SimpleDateFormat format=new SimpleDateFormat("HH:mm:ss");
            String time = (String) map.get("time");
            int i = time.indexOf('~');
            String start=time.substring(0,i);
            String end=time.substring(i+1,time.length());
            log.error("start -> {} , end -> {}",format.parse(start),format.parse(end));
            map.put("start",start);
            map.put("end",end);
        }
        IPage<RouteVo> routeInfo = queryService.getRouteInfo(map, currentPage, pageSize);
        RedisConstant.HOT.add(Integer.parseInt((String)map.get("toAddrId")));
        return routeInfo;
    }

    //查询低价城市
    @GetMapping("/lowPrice/search")
    public List<LowPriceRoute> lowPrice(@RequestParam("id")Integer id){
        return queryService.lowPrice(id);
    }

    //航班号查询
    @GetMapping("/flightNum/search")
    public IPage<RouteVo> flightNum(FlightNumVo vo, @RequestParam(value = "currentPage",defaultValue = "1")Integer currentPage,
                                    @RequestParam(value = "pageSize",defaultValue = "10")Integer pageSize) throws ParseException {
        int sendTime = DateUtil.dayOfWeek(vo.getSendTime());
        sendTime=sendTime-1;
        if(sendTime<0)sendTime=6;
        vo.setSendWeek(sendTime);
        return queryService.getRouteByFlightNum(vo, currentPage, pageSize);
    }

    //获取热门城市
    @GetMapping("/hot/city/search")
    @SuppressWarnings("all")
    public List<Map<String,Object>>  getHotCity() throws ParseException {
        long hour = System.currentTimeMillis() / (1000 * 60 * 60);
        Set set = this.redisTemplate.opsForZSet().reverseRangeWithScores("rank:week", 0, 7);
        if(set==null||set.size()<=1)throw new YueException("暂时还无热门城市哦");
        Iterator iterator = set.iterator();
        List<Integer> hotCitys=new ArrayList<>();
        while(iterator.hasNext()){
            ZSetOperations.TypedTuple<String> next = (ZSetOperations.TypedTuple<String>) iterator.next();
            hotCitys.add(Integer.valueOf(next.getValue()));
        }
        List<Map<String,Object>> lists=new ArrayList<>();
        SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd");
        for(Integer cityId:hotCitys){
            Map<String,Object> map=new HashMap<>();
            //查询城市详细信息
            City cityById = cityService.getCityById(cityId);
            int i = cityById.getCity().indexOf('(');
            if(i!=-1){
                cityById.setCity(cityById.getCity().substring(0,i));
            }
            //查询去另外热门城市的航线信息
            List<RouteVo> hot = queryService.getHotCitys(cityId, hotCitys);
            if(hot.size()==0)continue;
            for(RouteVo routeVo:hot){
                //将sendTime转换为日期
                //获取当前日期
                //获取今天周几
                Integer sendTime = routeVo.getSendTime();
                // 0 -> 周一 6 -> 周日
                int week = DateUtil.thisDayOfWeek()-1;
                //0 -> 周一 6-> 周日

                int tday=0;
                if(week>sendTime){
                    tday=6-week+sendTime;
                }
                else if(week<sendTime){
                    tday=sendTime-week;
                }
                //获取当天日期
                String today = DateUtil.today();
                Calendar calendar=Calendar.getInstance();
                calendar.setTime(format.parse(today));
                calendar.add(calendar.DATE,tday+1);
                routeVo.setDateTime(format.format(calendar.getTime()));
                int i1 = routeVo.getToCity().getCity().indexOf('(');
                if(i1!=-1){
                   routeVo.getToCity().setCity(routeVo.getToCity().getCity().substring(0,i1));
                }
                int i2 = routeVo.getFromCity().getCity().indexOf('(');
                if(i2!=-1){
                    routeVo.getFromCity().setCity(routeVo.getFromCity().getCity().substring(0,i2));
                }
            }
            map.put("fromCity",cityById);
            map.put("toCitys",hot);
            lists.add(map);
        }
        return lists;
    }

    /**
     * 疫情情况接口
     * @return
     */
    @GetMapping("/epidemic")
    public String epidemic() {
        return HttpUtil.get("https://c.m.163.com/ug/api/wuhan/app/data/list-total?t=" + System.currentTimeMillis());
    }

    @GetMapping("/route/byId/{id}")
    @NotControllerResponseAdvice
    public RouteEn routeById(@PathVariable("id")Integer id) throws ParseException {
        RouteEn routeById = queryService.getRouteById(id);
        return routeById;
    }

    //获取航班详细信息
    @GetMapping("/route/info")
    @NotControllerResponseAdvice
    public RouteEn getRouteInfo(@RequestParam("fromAddrId")String fromAddrId,@RequestParam("toAddrId")String toAddrId) throws ParseException {
        RouteEn routeInfoById = queryService.getRouteInfoById(fromAddrId, toAddrId);
        return routeInfoById;
    }

    //通过ids获取城市防疫信息
    @GetMapping("/route/sourceInfo/{ids}")
    public Object sourceInfo(@PathVariable("ids")String ids){
        List<String> idLists = Arrays.asList(ids.split(","));
        List<City> list = cityService.lambdaQuery().in(City::getId, idLists).list();
        return list;
    }

    //健康检测
    @GetMapping("/health")
    public String health(){
        return "success";
    }

    //终止服务
    @PostMapping("/shutdown")
    public void shutdown(){
        ConfigurableApplicationContext run = (ConfigurableApplicationContext) context;
        run.close();
    }

    //测试灰度发布
    @GetMapping("/test")
    public String test(){
        return "旧版本";
    }

    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        this.context=applicationContext;
    }
}
