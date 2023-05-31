package com.yue.spring.client;

import com.yue.spring.pojo.VO.RouteEn;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

@FeignClient("queryservice")
public interface RouteClient {

    @RequestMapping("query/route/byId/{id}")
    public RouteEn routeById(@PathVariable("id")Integer id);

    @GetMapping("query/route/info")
    public RouteEn getRouteInfo(@RequestParam("fromAddrId")String fromAddrId, @RequestParam("toAddrId")String toAddrId);
}
