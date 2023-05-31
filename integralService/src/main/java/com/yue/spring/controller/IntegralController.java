package com.yue.spring.controller;

import com.yue.spring.group.NotControllerResponseAdvice;
import com.yue.spring.handler.utils.OauthUtils;
import com.yue.spring.pojo.DO.Integral;
import com.yue.spring.pojo.DTO.IntegralDTO;
import com.yue.spring.pojo.PO.IntegralPo;
import com.yue.spring.pojo.VO.IntegralVo;
import com.yue.spring.service.IntegralService;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.web.bind.annotation.*;

import java.util.Date;

@RestController
@RequestMapping("/integral")
public class IntegralController implements ApplicationContextAware {

    @Autowired
    private IntegralService integralService;

    @Autowired
    private RabbitTemplate rabbitTemplate;

    private ApplicationContext context;

    //添加积分流水
    @PostMapping("/add")
    public String add(@RequestBody IntegralDTO integralDTO){
        Integral integral=new Integral();
        BeanUtils.copyProperties(integralDTO,integral);
        integral.setUserId(Integer.valueOf(OauthUtils.getCurrentUser().getUserId()));
        integralService.save(integral);
        return "添加成功";
    }

    //获取用户积分
    @GetMapping("/get")
    public IntegralVo get(){
        IntegralPo userIntegral = integralService.getUserIntegral(Integer.valueOf(OauthUtils.getCurrentUser().getUserId()));
        return IntegralVo.builder()
                .integralPo(userIntegral)
                .build();
    }

    //远程调用
    @GetMapping("/getVo")
    @NotControllerResponseAdvice
    public IntegralVo getVo(){
        IntegralPo userIntegral = integralService.getUserIntegral(Integer.valueOf(OauthUtils.getCurrentUser().getUserId()));
        return IntegralVo.builder()
                .integralPo(userIntegral)
                .build();
    }

    //健康检测
    @GetMapping("/health")
    public String health() {
        return "success";
    }

    //服务停止
    @PostMapping("/shutdown")
    public void shutdown() {
        ConfigurableApplicationContext run = (ConfigurableApplicationContext) context;
        run.close();
    }

    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        this.context = applicationContext;
    }
}
