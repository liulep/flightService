package com.yue.spring.controller;

import com.yue.spring.pojo.Stock;
import com.yue.spring.service.StockService;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("stock")
public class StockController implements ApplicationContextAware {

    private ApplicationContext context;
    @Autowired
    private StockService stockService;

    //检测健康
    @GetMapping("/health")
    public String health(){
        return "true";
    }

    //停止服务
    @PostMapping("/shutdown")
    public void shutdown(){
        ConfigurableApplicationContext run = (ConfigurableApplicationContext) context;
        run.close();
    }

    //减少库存
    @PostMapping("/reduced/stock")
    public String reduced(@RequestBody int[] ids){
        //ids 航班ids
        return "....";
    }

    //获取库存
    @GetMapping("/get/stockById")
    public Stock get(int id){
        return stockService.getOne(stockService.lambdaQuery().eq(Stock::getLineId,id));
    }

    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        this.context=applicationContext;
    }
}
