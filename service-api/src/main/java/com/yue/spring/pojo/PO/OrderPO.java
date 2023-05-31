package com.yue.spring.pojo.PO;

import com.yue.spring.pojo.DO.Order;
import com.yue.spring.pojo.DO.OrderInfo;
import lombok.*;
import lombok.experimental.SuperBuilder;

import java.io.Serializable;
import java.util.List;

@Setter
@Getter
@SuperBuilder
public class OrderPO extends Order implements Serializable {

    private List<OrderInfoPO> orderInfos; //子订单
}
