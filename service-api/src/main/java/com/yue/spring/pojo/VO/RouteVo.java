package com.yue.spring.pojo.VO;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.yue.spring.pojo.City;
import com.yue.spring.pojo.Route;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;

import java.io.Serializable;
import java.util.Date;

@AllArgsConstructor
@NoArgsConstructor
@Data
@JsonIgnoreProperties(value = {"handler"})
public class RouteVo extends Route implements Serializable{

    private static final long serialVersionUID=986823857621547284L;

    private City fromCity;

    private City toCity;

    private Integer hour;

    private Integer nextId;

    private Integer nextFromAddrId;

    private Integer nextToAddrId;

    private String nextFlightNum;

    private String nextModel;

    @JsonFormat(pattern = "HH:mm:ss")
    @DateTimeFormat(pattern = "HH:mm:ss")
    private Date nextStartTime;

    private String nextStartAirport;

    @JsonFormat(pattern = "HH:mm:ss")
    @DateTimeFormat(pattern = "HH:mm:ss")
    private Date nextEndTime;

    private String nextEndAirport;

    private Double nextPunctuality;

    private Integer nextSendTime;

    private Double nextPrice;

    private City nextToCity;

    private City nextFromCity;

    private Integer middleHour;

    private Integer nextHour;

    private Double totalPrice;

    private String dateTime;

    private String nextDateTime;

    private String nextSource;

    private String nextInfo;

}
