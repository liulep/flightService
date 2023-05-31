package com.yue.spring.pojo.DTO;

import com.baomidou.mybatisplus.annotation.TableField;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.yue.spring.pojo.DO.Order;
import lombok.Getter;
import lombok.Setter;
import org.springframework.format.annotation.DateTimeFormat;

import javax.validation.constraints.NotBlank;
import java.io.Serializable;
import java.util.Date;

@Getter
@Setter
public class OrderDTO extends Order implements Serializable {

    @NotBlank(message = "航班Id不能为空")
    private String lineIds; //航班Ids

    private Integer couponId; //优惠卷Id

    @NotBlank(message = "联系人不能为空")
    private String userRelationIds; //用户联系人Ids

    @DateTimeFormat(pattern = "yyyy-MM-dd")
    private Date fromTime; //第一趟起始时间

    @DateTimeFormat(pattern = "yyyy-MM-dd")
    private Date toTime; //第二趟起始时间

    private Integer type; //航班类型

    @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss",timezone = "GMT+8")
    private Date timeout;

    private Double price;
}
