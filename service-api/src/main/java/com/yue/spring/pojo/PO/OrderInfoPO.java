package com.yue.spring.pojo.PO;

import com.yue.spring.pojo.DO.OrderInfo;
import com.yue.spring.pojo.Relation;
import com.yue.spring.pojo.Route;
import com.yue.spring.pojo.VO.RouteEn;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.SuperBuilder;

import java.io.Serializable;
import java.util.Date;

@Setter
@Getter
@SuperBuilder
public class OrderInfoPO extends OrderInfo implements Serializable {

    private RouteEn route; //航班

    private Relation relation; //联系人

}
