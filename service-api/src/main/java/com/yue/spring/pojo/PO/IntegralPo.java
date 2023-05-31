package com.yue.spring.pojo.PO;

import com.yue.spring.pojo.DO.Integral;
import com.yue.spring.pojo.User;
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
public class IntegralPo extends Integral implements Serializable {

    //用户信息
    private User user;
}
