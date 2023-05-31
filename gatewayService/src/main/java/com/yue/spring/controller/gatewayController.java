package com.yue.spring.controller;

import com.yue.spring.pojo.R;
import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/gateway")
public class gatewayController implements ApplicationContextAware {

    private ApplicationContext context;

    //检测健康
    @GetMapping("/health")
    public R health(){
        return R.ok().data("success");
    }

    //停止服务
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
