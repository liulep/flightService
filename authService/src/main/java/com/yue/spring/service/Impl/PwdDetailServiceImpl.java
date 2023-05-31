package com.yue.spring.service.Impl;

import cn.hutool.core.util.ArrayUtil;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.yue.spring.mapper.UserAuthMapper;
import com.yue.spring.pojo.SecurityUser;
import com.yue.spring.pojo.User;
import com.yue.spring.service.PwdDetailService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.authority.AuthorityUtils;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

@Service
public class PwdDetailServiceImpl implements PwdDetailService {

    @Autowired
    private UserAuthMapper userAuthMapper;

    @Override
    public UserDetails loadUserByMobile(String username) throws UsernameNotFoundException {
        User user = userAuthMapper.selectOne(new QueryWrapper<User>().eq("user_name", username));
        if (Objects.isNull(user))
            throw new UsernameNotFoundException("账号不存在！");
        //该用户的所有权限（角色）
        List<String> roles=new ArrayList<>();
        roles.add("user");
        return SecurityUser.builder()
                .userId(String.valueOf(user.getId()))
                .username(user.getUserName())
                .password(user.getPassword())
                .authorities(AuthorityUtils.createAuthorityList(ArrayUtil.toArray(roles,String.class)))
                .build();
    }
}
