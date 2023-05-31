package com.yue.spring.pojo.VO;

import com.yue.spring.pojo.PO.CouponFlowPO;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.SuperBuilder;
import net.bytebuddy.implementation.bind.annotation.Super;

@Setter
@Getter
@SuperBuilder
public class CouponVo {
    private CouponFlowPO couponFlowPO;
}
