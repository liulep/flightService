package com.yue.spring.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.yue.spring.pojo.Relation;
import com.yue.spring.pojo.User;
import com.yue.spring.pojo.po.UserCode;

import java.util.List;

public interface UserService extends IService<User> {
    //修改用户信息
    User updateUserInfo(UserCode user);

    //获取联系人列表
    Page<Relation> relationListByUserId(Integer userId, Integer currentPage, Integer pageSize);

    //发送邮件
    void sendMail(String email);
}
