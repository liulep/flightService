package com.yue.spring.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.yue.spring.pojo.User;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

@Mapper
public interface UserAuthMapper extends BaseMapper<User> {

    @Select("select DISTINCT to_addr_id from route  where from_addr_id=#{id} order by to_addr_id ")
    int []  queryFromCity(Integer id);

    @Select("select DISTINCT from_addr_id from route  where to_addr_id=#{id} order by from_addr_id ")
    int []  queryToCity(Integer id);
}
