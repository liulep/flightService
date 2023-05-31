package com.yue.spring.pojo.VO;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class LowPriceRoute implements Serializable {

    private static final long serialVersionUID=986823857621547245L;

    private Integer fromAddrId;

    private Integer toAddrId;

    private String fromCityName;

    private String toCityName;

    private String toAbbreviation;

    private String fromAbbreviation;

    private String dateTime;

    private Integer sendTime;

    private String Image;

    private Double price;
}
