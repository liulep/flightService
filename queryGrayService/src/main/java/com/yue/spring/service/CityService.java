package com.yue.spring.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.yue.spring.pojo.City;

public interface CityService extends IService<City> {

    City getCityById(Integer id);

}
