package com.yue.spring.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.yue.spring.pojo.DO.CouponFlow;
import com.yue.spring.pojo.PO.CouponFlowPO;

import java.util.List;

public interface CouponFlowService extends IService<CouponFlow> {

    public List<CouponFlowPO> getUserCoupon(Integer userId);

    public void updateCouponStatus();
}
