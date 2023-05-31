package com.yue.spring.pojo;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class Route {

    private String id;

    private List<Predicate> predicates=new ArrayList<>();

    private List<Filter> filters=new ArrayList<>();

    private String uri;

    private int order;
}
