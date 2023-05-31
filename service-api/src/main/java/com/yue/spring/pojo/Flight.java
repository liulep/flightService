package com.yue.spring.pojo;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class Flight implements Serializable {

    private static final long serialVersionUID=986823857621547282L;

    private Integer id;

    private String flightName;

}
