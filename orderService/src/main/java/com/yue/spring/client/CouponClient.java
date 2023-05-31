package com.yue.spring.client;

import com.yue.spring.pojo.DTO.CouponDTO;
import com.yue.spring.pojo.PO.CouponFlowPO;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

@FeignClient("couponservice")
public interface CouponClient {

    @RequestMapping("coupon/couponInfo")
    public CouponFlowPO couponInfo(@RequestParam("id")Integer id);

    @RequestMapping("coupon/use")
    public boolean use(@RequestParam("id")Integer id);

}
