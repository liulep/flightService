package com.yue.spring.pojo.VO;

import com.yue.spring.pojo.PlaneOrder;
import com.yue.spring.pojo.Relation;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class OrderVo extends PlaneOrder {

    private List<Relation> userRelations;

    private List<RouteEn> lines;

}
