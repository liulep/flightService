package com.yue.spring.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.yue.spring.pojo.DO.Integral;
import com.yue.spring.pojo.PO.IntegralPo;
import com.yue.spring.pojo.VO.IntegralVo;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

@Mapper
public interface IntegralMapper extends BaseMapper<Integral> {

    IntegralPo getUserIntegral(@Param("userId") Integer userId);
}
