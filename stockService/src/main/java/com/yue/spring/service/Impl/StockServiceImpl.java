package com.yue.spring.service.Impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.yue.spring.mapper.StockMapper;
import com.yue.spring.pojo.Stock;
import com.yue.spring.service.StockService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class StockServiceImpl extends ServiceImpl<StockMapper, Stock> implements StockService {

    @Autowired
    private StockMapper stockMapper;

    @Override
    public List<Stock> getStockByWeek(int sendWeek) {
        return stockMapper.getStockByWeek(sendWeek);
    }
}
