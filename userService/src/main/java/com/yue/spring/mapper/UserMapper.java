package com.yue.spring.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.yue.spring.pojo.Relation;
import com.yue.spring.pojo.User;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

@Mapper
public interface UserMapper extends BaseMapper<User> {

    @Select("select * from relation where user_id=#{userId} and delete_flag=0")
    Page<Relation> relationListByUserId(@Param("page") Page<?> page,@Param("userId")Integer userId);
}
