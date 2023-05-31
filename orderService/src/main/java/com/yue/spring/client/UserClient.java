package com.yue.spring.client;

import com.alibaba.fastjson.JSON;
import com.yue.spring.pojo.R;
import com.yue.spring.pojo.Relation;
import com.yue.spring.pojo.User;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;

@FeignClient("userservice")
public interface UserClient {

    @RequestMapping("user/user_relation/{id}")
    public Relation getRelationById(@PathVariable("id")Integer id);

    @RequestMapping("user/user_info")
    public R getUserInfo();
}
