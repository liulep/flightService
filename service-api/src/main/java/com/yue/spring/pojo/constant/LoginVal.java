package com.yue.spring.pojo.constant;

import com.yue.spring.pojo.User;
import lombok.Data;

/**
 * 保存登录用户的信息
 */
@Data
public class LoginVal extends JwtInformation{

    private String userId;

    private String username;

    private User user;

    private String[] authorities;
}
