package com.yue.spring.pojo;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class CityRelations implements Serializable {

    private static final long serialVersionUID=1L;

    @TableId(type=IdType.AUTO)
    private Integer cityId;

    private String toCityIds;

    private String transferCityIds;
}
