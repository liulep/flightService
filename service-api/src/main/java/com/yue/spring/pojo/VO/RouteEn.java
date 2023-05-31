package com.yue.spring.pojo.VO;

import com.yue.spring.pojo.City;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class RouteEn implements Serializable {

    private static final long serialVersionUID=98682385762154756L;

    private Integer id;

    private Integer fromAddrId;

    private Integer toAddrId;

    private String flightNum;

    private String model;

    private String startTime;

    private String startAirport;

    private String endTime;

    private String endAirport;

    private Double punctuality;

    private Integer sendTime;

    private Double price;

    private City fromCity;

    private City toCity;

    private Integer hour;

    private Integer nextId;

    private Integer nextFromAddrId;

    private Integer nextToAddrId;

    private String nextFlightNum;

    private String nextModel;

    private String nextStartTime;

    private String nextStartAirport;

    private String nextEndTime;

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


}
