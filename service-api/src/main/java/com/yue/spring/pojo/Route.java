package com.yue.spring.pojo;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;

import java.io.Serializable;
import java.util.Date;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class Route implements Serializable {

    private static final long serialVersionUID=986823857621547284L;

    private Integer id;

    private Integer fromAddrId;

    private Integer toAddrId;

    private String flightNum;

    private String model;

    @JsonFormat(pattern = "HH:mm:ss")
    @DateTimeFormat(pattern = "HH:mm:ss")
    private Date startTime;

    private String startAirport;

    @JsonFormat(pattern = "HH:mm:ss")
    @DateTimeFormat(pattern = "HH:mm:ss")
    private Date endTime;

    private String endAirport;

    private Double punctuality;

    private Integer sendTime;

    private Double price;

}
