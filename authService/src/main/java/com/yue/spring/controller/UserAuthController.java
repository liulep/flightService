package com.yue.spring.controller;

import com.yue.spring.AuthApplication;
import com.yue.spring.pojo.constant.LoginVal;
import com.yue.spring.pojo.constant.SysConstant;
import com.yue.spring.handler.utils.OauthUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.concurrent.TimeUnit;

@RestController
@RequestMapping("oauth")
@Slf4j
public class UserAuthController implements ApplicationContextAware {

    @Autowired
    private StringRedisTemplate redisTemplate;

    private ApplicationContext context;


    @PostMapping("/logout")
    public String logout(){
        LoginVal loginVal = OauthUtils.getCurrentUser();
        redisTemplate.opsForValue().set(SysConstant.JTI_KEY_PREFIX+loginVal.getJti(),"",loginVal.getExpireIn(), TimeUnit.SECONDS);
        return "注销成功";
    }


    @GetMapping("/health")
    public String health(){
        return "success";
    }

    @PostMapping("/shutdown")
    public void shutdown(){
        ConfigurableApplicationContext run = (ConfigurableApplicationContext) context;
        run.close();
    }

    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        this.context=applicationContext;
    }
}
