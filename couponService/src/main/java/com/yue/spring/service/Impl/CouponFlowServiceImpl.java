package com.yue.spring.service.Impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.yue.spring.mapper.CouponFlowMapper;
import com.yue.spring.pojo.DO.CouponFlow;
import com.yue.spring.pojo.PO.CouponFlowPO;
import com.yue.spring.service.CouponFlowService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class CouponFlowServiceImpl extends ServiceImpl<CouponFlowMapper, CouponFlow> implements CouponFlowService {

    @Autowired
    private CouponFlowMapper couponFlowMapper;

    @Override
    public List<CouponFlowPO> getUserCoupon(Integer userId) {
        return couponFlowMapper.getUserCoupon(userId);
    }

    @Override
    @Async
    public void updateCouponStatus() {
        couponFlowMapper.updateCouponStatus();
    }
}
