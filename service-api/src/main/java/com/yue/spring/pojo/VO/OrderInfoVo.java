package com.yue.spring.pojo.VO;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.yue.spring.pojo.PO.OrderInfoPO;
import com.yue.spring.pojo.PO.OrderPO;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;
import org.springframework.format.annotation.DateTimeFormat;

import java.util.Date;
import java.util.List;

@Setter
@Getter
@Builder
public class OrderInfoVo  {

    private OrderPO orderPO; //主订单

    @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss",timezone = "GMT+8")
    private Date timeOut; //过期时间

    private Long dayNum; //出行天数
}
