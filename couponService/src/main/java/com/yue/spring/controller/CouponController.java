package com.yue.spring.controller;

import cn.hutool.core.date.DateTime;
import cn.hutool.core.date.DateUtil;
import com.yue.spring.Exception.YueException;
import com.yue.spring.client.IntegralClient;
import com.yue.spring.group.NotControllerResponseAdvice;
import com.yue.spring.handler.utils.OauthUtils;
import com.yue.spring.pojo.DO.Coupon;
import com.yue.spring.pojo.DO.CouponFlow;
import com.yue.spring.pojo.DO.Integral;
import com.yue.spring.pojo.DTO.CouponDTO;
import com.yue.spring.pojo.PO.CouponFlowPO;
import com.yue.spring.pojo.VO.CouponVo;
import com.yue.spring.pojo.VO.IntegralVo;
import com.yue.spring.service.CouponFlowService;
import com.yue.spring.service.CouponService;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.context.ApplicationEventPublisherAware;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.web.bind.annotation.*;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

@RestController
@RequestMapping("/coupon")
public class CouponController implements ApplicationContextAware {

    private ApplicationContext context;

    @Autowired
    private CouponService couponService;

    @Autowired
    private IntegralClient integralClient;

    @Autowired
    private RabbitTemplate rabbitTemplate;

    @Autowired
    private CouponFlowService couponFlowService;

    //获取所有优惠卷
    @GetMapping("/getAll")
    public List<Coupon> all(){
        List<Coupon> couponList = couponService.list();
        couponFlowService.updateCouponStatus();
        return couponList;
    }

    //使用积分兑换优惠卷
    @PostMapping("/exchange")
    public String exchange(@RequestBody CouponDTO couponDTO){
        //获取用户积分
        IntegralVo vo = integralClient.getVo();
        //获取优惠卷
        Coupon coupon = couponService.getById(couponDTO.getId());
        if(vo.getIntegralPo().getIntegral()<coupon.getIntegral()){
            throw new YueException("积分不够,兑换失败");
        }
        else{
            Calendar rightNow = Calendar.getInstance();
            rightNow.setTime(new Date());
            rightNow.add(Calendar.DAY_OF_YEAR,coupon.getDay());//过期时间
            //领取优惠卷
            couponFlowService.save(CouponFlow.builder()
                    .userId(vo.getIntegralPo().getUserId())
                    .couponId(coupon.getId())
                    .status(0)
                    .expirationTime(rightNow.getTime())
                    .collectionTime(new Date())
                    .build());
            //扣除积分
            rabbitTemplate.convertAndSend("article_exchange","article_key", Integral.builder()
                    .userId(vo.getIntegralPo().getUserId())
                    .integral(coupon.getIntegral()*-1)
                    .createTime(new Date())
                    .build());
        }
        return "兑换成功";
    }

    //获取用户优惠卷
    @GetMapping("/getUserCoupon")
    public List<CouponFlowPO> getUserCoupon(){
        List<CouponFlowPO> userCoupon =
                couponFlowService.getUserCoupon(Integer.valueOf(OauthUtils.getCurrentUser().getUserId()));
        couponFlowService.updateCouponStatus();
        return userCoupon;
    }

    //获取优惠卷信息
    @GetMapping("/couponInfo")
    @NotControllerResponseAdvice
    public CouponFlowPO couponInfo(@RequestParam("id")Integer id){
        CouponFlow byId = couponFlowService.getById(id);
        Coupon byId1 = couponService.getById(byId.getCouponId());
        CouponFlowPO build = CouponFlowPO.builder()
                .coupon(byId1)
                .build();
        BeanUtils.copyProperties(byId,build);
        return build;
    }

    //使用优惠卷
    @GetMapping ("/use")
    @NotControllerResponseAdvice
    public boolean use(@RequestParam("id")Integer id){
        CouponFlow byId = couponFlowService.getById(id);
        byId.setStatus(1);
        byId.setUsageTime(new Date());
        couponFlowService.updateById(byId);
        return true;
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
        this.context=applicationContext;
    }
}
