package com.yue.spring.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.yue.spring.pojo.Stock;

import java.util.List;

public interface StockService extends IService<Stock> {

    List<Stock> getStockByWeek(int sendWeek);
}
