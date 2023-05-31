package com.yue.spring.client;

import com.yue.spring.pojo.Stock;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;

@FeignClient("stockservice")
public interface StockClient {

    @GetMapping("/order/get/stockById")
    public Stock get(int id);

}
