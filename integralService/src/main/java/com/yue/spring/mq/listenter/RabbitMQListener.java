package com.yue.spring.mq.listenter;

import com.yue.spring.mq.config.RabbitMqConfig;
import com.yue.spring.pojo.DO.Integral;
import com.yue.spring.pojo.PO.IntegralPo;
import com.yue.spring.service.IntegralService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.amqp.core.Message;
import com.rabbitmq.client.Channel;

import java.io.IOException;

@SuppressWarnings("ALL")
@Component
@Slf4j
public class RabbitMQListener {

    @Autowired
    private IntegralService integralService;

    @RabbitListener(queues = RabbitMqConfig.QUEUE_NAME)
    public void consumerArticleQueue(Message message, Channel channel,Integral integral) throws IOException {
        channel.basicAck(message.getMessageProperties().getDeliveryTag(),false);
        //赠送积分
        if(integral.getIntegral()<0){
            IntegralPo userIntegral = integralService.getUserIntegral(integral.getUserId());
            if(userIntegral.getIntegral()<=Math.abs(integral.getIntegral())){
                integral.setIntegral(userIntegral.getIntegral()*-1);
                integralService.save(integral);
            }
        }else {
            integralService.save(integral);
        }
    }

}
