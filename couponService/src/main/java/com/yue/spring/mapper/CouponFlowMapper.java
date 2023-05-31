package com.yue.spring.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.yue.spring.pojo.DO.CouponFlow;
import com.yue.spring.pojo.PO.CouponFlowPO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface CouponFlowMapper extends BaseMapper<CouponFlow> {

    public List<CouponFlowPO> getUserCoupon(@Param("userId") Integer userId);

    public Integer updateCouponStatus();
}
