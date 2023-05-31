package com.yue.spring.pojo;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.*;

import java.io.Serializable;
import java.util.Date;

@Data
@AllArgsConstructor
@ToString
@NoArgsConstructor
@Builder
@TableName("city_detail")
public class City implements Serializable {

    private static final long serialVersionUID=986823857621547281L;

    private Integer id;

    private String city;

    private String cityEn;

    private String abbreviation;

    private String airport;

    @TableField(value = "`describe`")
    private String describe;

    private Date createTime;

    private Date updateTime;

    private Integer hot;

    private String image;

    private String source;

    private String info;
}
