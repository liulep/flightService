package com.yue.spring.mq.listener;

import com.rabbitmq.client.Channel;
import com.yue.spring.client.RouteClient;
import com.yue.spring.mq.config.RabbitMQConfig;
import com.yue.spring.pojo.DO.Order;
import com.yue.spring.pojo.DO.OrderInfo;
import com.yue.spring.pojo.NonWebRequestAttributes;
import com.yue.spring.pojo.VO.RouteEn;
import com.yue.spring.pojo.DTO.*;
import com.yue.spring.service.OrderInfoService;
import com.yue.spring.service.OrderService;
import com.yue.spring.utils.SequenceUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;

import java.text.SimpleDateFormat;
import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;

@Component
@Slf4j
@SuppressWarnings("all")
public class RabbitMQListener{

    @Autowired
    private OrderService orderService;

    @Autowired
    private OrderInfoService orderInfoService;

    @Autowired
    private SequenceUtils sequenceUtils;

    @Autowired
    private RedisTemplate redisTemplate;

    @Autowired
    private RabbitTemplate rabbitTemplate;

    @Autowired
    private RouteClient routeClient;

    private final static String ORDER_INFO="order_num_user:";

    private final static String ORDER_USER="order_user_list:";

    private final static String INIT_STOCK="line_stock:";


    @RabbitListener(queues = RabbitMQConfig.QUEUE_NAME)
    public void onMessage(OrderDTO planeOrder, Message message, Channel channel) throws Exception {
        channel.basicAck(message.getMessageProperties().getDeliveryTag(),false);
        if(planeOrder==null)return;
        //解决异步调用上下文信息丢失
        NonWebRequestAttributes request=new NonWebRequestAttributes();
        Map<String, Object> headers = message.getMessageProperties().getHeaders();
        for(String key:headers.keySet()){
            request.setAttribute(key,headers.get(key),0);
        }
        RequestContextHolder.setRequestAttributes(request);
        planeOrder.setStatus(1);
        planeOrder.setCreateTime(new Date(System.currentTimeMillis()));
        planeOrder.setTimeout(new Date(System.currentTimeMillis()+1000*60*10));
        planeOrder.setDepartureDate(planeOrder.getFromTime());
        //计算价格
        AtomicReference<Double> price= new AtomicReference<>((double) 0);
        Arrays.stream(planeOrder.getLineIds().split(",")).forEach( id->{
            RouteEn routeEn = routeClient.routeById(Integer.parseInt(id));
            price.updateAndGet(v -> v + routeEn.getPrice());
        });
        planeOrder.setTotalPrice(price.get()*planeOrder.getUserRelationIds().split(",").length);
        redisTemplate.opsForValue().set(ORDER_USER+ planeOrder.getUserId(),planeOrder,610, TimeUnit.SECONDS);
        redisTemplate.opsForValue().set(ORDER_INFO+ planeOrder.getId(),planeOrder,610, TimeUnit.SECONDS);
        //将等待支付的订单加入等待队列中
        rabbitTemplate.convertAndSend(RabbitMQConfig.EXCHANGE_NAME,"ttl.order",planeOrder,RabbitMQConfig.messagePostProcessor);
    }

    @RabbitListener(queues = RabbitMQConfig.DEAD_LETTER_QUEUE_NAME)
    public void onMessage(Message message, Channel channel,OrderDTO planeOrder_user) throws Exception {
        channel.basicAck(message.getMessageProperties().getDeliveryTag(),false);
        SimpleDateFormat format=new SimpleDateFormat("yyyy-MM-dd");
        Order byId = orderService.getById(planeOrder_user.getId());
        if(planeOrder_user==null)return ;
        else if(byId!=null){
            //订单已被支付或者已经取消
            return;
        }
        //插入主订单
        Order order=new Order();
        BeanUtils.copyProperties(planeOrder_user,order);
        order.setStatus(0);
        orderService.save(order);
        //插入子订单
        List<OrderInfo> orderInfos=new ArrayList<>();
        int length = planeOrder_user.getUserRelationIds().split(",").length;
        String[] date = new String[]{format.format(planeOrder_user.getFromTime()), planeOrder_user.getToTime() == null ? null : format.format(planeOrder_user.getToTime())};
        int i=0;
        for(String lineId:planeOrder_user.getLineIds().split(",")){
            String s=date[i];
            if (s==null) s=date[0];
            //归还库存
            redisTemplate.opsForValue().increment(INIT_STOCK +s+":"+ lineId,length);
            for(String userRelationId:planeOrder_user.getUserRelationIds().split(",")){
                OrderInfo orderInfo=new OrderInfo();
                orderInfo.setOrderId(order.getId());
                orderInfo.setLineId(Integer.valueOf(lineId));
                orderInfo.setUserRelationId(Integer.valueOf(userRelationId));
                orderInfo.setDepartureDate(new SimpleDateFormat("yyyy-MM-dd").parse(s));
                orderInfos.add(orderInfo);
            }
            i++;
        }
        orderInfoService.saveBatch(orderInfos);
    }
}
