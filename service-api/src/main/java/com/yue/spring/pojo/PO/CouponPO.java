package com.yue.spring.pojo.PO;


import com.yue.spring.pojo.DO.Coupon;
import com.yue.spring.pojo.User;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;

import java.io.Serializable;

@Getter
@Setter
@SuperBuilder
public class CouponPO extends Coupon implements Serializable {

}
