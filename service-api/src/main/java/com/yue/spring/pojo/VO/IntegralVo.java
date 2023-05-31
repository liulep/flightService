package com.yue.spring.pojo.VO;

import com.yue.spring.pojo.PO.IntegralPo;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;

import java.io.Serializable;

@Getter
@Setter
@SuperBuilder
@AllArgsConstructor
@NoArgsConstructor
public class IntegralVo implements Serializable {

    private IntegralPo integralPo;
}
