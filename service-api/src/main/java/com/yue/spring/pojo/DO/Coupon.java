package com.yue.spring.pojo.DO;

import cn.hutool.db.DaoTemplate;
import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;
import org.springframework.format.annotation.DateTimeFormat;

import java.io.Serializable;
import java.util.Date;

@Data
@AllArgsConstructor
@NoArgsConstructor
@SuperBuilder
public class Coupon implements Serializable {

    private static final long serialVersionUID=996823857621547284L;

    private Integer id;

    private String couponName;

    private Long fullMoney;

    private Long minusMoney;

    private String routeId;

    private Long integral;

    private Integer day;

    @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private Date createTime;
}
