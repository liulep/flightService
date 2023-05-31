package com.yue.spring.entity;

import lombok.Data;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * 路由断言实体类
 *
 */
@Data
public class PredicateEntity {
  // 断言对应的Name
  private String name;

  // 断言规则
  private Map<String, String> args = new LinkedHashMap<>();

}
