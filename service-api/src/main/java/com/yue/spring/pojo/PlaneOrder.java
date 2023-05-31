package com.yue.spring.pojo;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;

import javax.validation.constraints.NotBlank;
import java.io.Serializable;
import java.util.Date;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
@TableName("plane_order")
public class PlaneOrder implements Serializable {

    private static final long serialVersionUID=986823857621547283L;

    private String id;

    private Integer userId;

    @NotBlank(message = "航班Id不能为空")
    private String lineIds;

    @NotBlank(message = "联系人不能为空")
    private String userRelationIds;

    private Integer status;

    private Double price;

    @DateTimeFormat(pattern = "yyyy-MM-dd")
    @JsonFormat(pattern = "yyyy-MM-dd")
    private Date departureDate;

    @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private Date createTime;

    @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss",timezone = "GMT+8")
    @TableField(exist = false)
    private Date timeout;

    @TableField(exist = false)
    private Integer type;

    @TableField(exist = false)
    @DateTimeFormat(pattern = "yyyy-MM-dd")
    private Date fromTime;

    @TableField(exist = false)
    @DateTimeFormat(pattern = "yyyy-MM-dd")
    private Date toTime;

    @TableField(exist = false)
    private Class<? extends NonWebRequestAttributes> attributes;

}
